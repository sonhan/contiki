package com.example.dynamicsocial;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ch.ethz.inf.vs.californium.coap.MessageObserverAdapter;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.CoAP.Code;
import ch.ethz.inf.vs.californium.coap.Response;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity implements OnClickListener {
	
	private ListView list;
	private EditText input;
	private Button send;

	private MyListAdapter adapter;// = new MyListAdapter(this, firstVals, secondVals);
	private ArrayList<ListRowModel> data = new ArrayList();
	
	Date date;
	SimpleDateFormat ft = new SimpleDateFormat ("hh:mm:ss");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		list = (ListView) findViewById(R.id.list);
		input = (EditText) findViewById(R.id.input);
		send = (Button) findViewById(R.id.send);
		
		adapter = new MyListAdapter(this, data);
		
		list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		list.setAdapter(adapter);
		send.setOnClickListener(this);
		
		//input.setText(Environment.getExternalStorageDirectory().getAbsolutePath());
		
		discover(lightURI);
		discover(tempURI);
		discover(boardingURI);
		
		
		//
	}
	
	private final String lightURI = "coap://[aaaa::212:7400:13cb:44bb]:5683";
	private final String tempURI = "coap://[aaaa::c30c:0:0:0]:5683";
	private final String boardingURI = "coap://[aaaa::212:7400:13cb:101a]:5683";
	
	private final String lightName = "Waiting Lounge";
	private final String tempName = "Aircraft";
	private final String boardingName = "Boarding Gate 16";
	
	private final String lightIcon = "waitingroom";
	private final String tempIcon = "plane";
	private final String boardingIcon = "boarding";
	
	private Request request1 = new Request(Code.GET);
	private Request request2 = new Request(Code.GET);
	private Request request3 = new Request(Code.GET);
	
	private void discover(String uri) {
		Request request = new Request(Code.GET);
		request.setURI(uri + "/.well-known/core");
		request.addMessageObserver(new DiscoveryObserver());
		execute(request);
	}
	
	private class DiscoveryObserver extends MessageObserverAdapter {
		public void onResponse(Response response) {
			final String source = response.getSource().toString();
			Log.i("bossie", response.getPayloadString());
			
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					date = new Date();
					String pre = "[" + ft.format(date) + "] ";
					if (lightURI.contains(source.substring(1)))
						adapter.add(new ListRowModel(lightName, pre + " Joined! Hello there!", lightIcon));
					if (tempURI.contains(source.substring(1))) {
						adapter.add(new ListRowModel(tempName, pre + " Joined! Hello there!", tempIcon));
						observe(tempURI + "/pushing/temperature", request2);
					}
					if (boardingURI.contains(source.substring(1))) {
						adapter.add(new ListRowModel(boardingName, pre + " Joined! Hello there!", boardingIcon));
						observe(boardingURI + "/sensors/button", request3);
					}
				}
			};
			
			runOnUiThread(runnable);
		}
	}
	private void observe(String uri, Request request) {
		request.setObserve();
		request.addMessageObserver(new ResponsePrinter());
		request.setPayload("some payload");
		request.setURI(uri);
		execute(request);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			request1.cancel();
			request2.setObserveCancel();
			request2.cancel();
			execute(request2);
			request3.setObserveCancel();
			request3.cancel();
			execute(request3);
			
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	int count = 0;
	
	@Override
	public void onClick(View v) {
		String inputText = input.getText().toString();
		date = new Date();
		String pre = "[" + ft.format(date) + "] ";
		adapter.add(new ListRowModel("Nadia", pre + inputText, "nadia"));
		adapter.notifyDataSetChanged();
		input.setText("");
		
		if (nlp(inputText)) {
			Request request = new Request(Code.GET);
			request.addMessageObserver(new ResponsePrinter());
			request.setPayload("some payload");
			request.setURI(lightURI + "/sensors/light");
			execute(request);
		}
		adapter.notifyDataSetChanged();
		
	}
	
	
	private boolean nlp(String text) {
		if (text.contains("light") || text.contains("suitable")) return true;
		else return false;
	}
	
	
	private void execute(Request request) {
		try {
			request.send();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private class ResponsePrinter extends MessageObserverAdapter {
		@Override
		public void onResponse(final Response response) {
			Log.i ("bossie", response.getPayloadString());
			Runnable mRunnable = new Runnable() {
			    @Override
			    public void run() {
			    	String res = response.getPayloadString();
			    	date = new Date();
					String pre = "[" + ft.format(date) + "] ";
					
			    	if (res.startsWith("EVENT")) {
			    		adapter.add(new ListRowModel(boardingName, pre + "Boarding: " + ++count, boardingIcon));
			    	} else if (res.startsWith("LIGHT")) {
			    		String text;
			    		String[] s = res.split(":");
			    		int val = Integer.valueOf(s[1]);
			    		
			    		if (val > 150)
			    			text = "Recommend to read! " + res;
			    		else 
			    			text = "Recommend not to read " + res;
			    		adapter.add(new ListRowModel(lightName, pre + text, lightIcon));
			    	} else if (res.startsWith("TEM")) {
			    		String[] s = res.split(":");
			    		data.add(new ListRowModel(tempName, pre + "Temperature: " + s[1] + " oC", tempIcon));
			    	}
			    	
			    	adapter.notifyDataSetChanged();
			    }
			};
			runOnUiThread(mRunnable);
		}
	}
	
}
