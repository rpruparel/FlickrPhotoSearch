package com.example.inclass;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends Activity {

	Switch parserSwitch;
	EditText searchEditText;
	Button go;
	ImageView next;
	ImageView previous;
	ImageView image;
	String flickrUrl = "https://api.flickr.com/services/rest";

	ArrayList < Photo > photoUrls;

	int counter = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		parserSwitch = (Switch) findViewById(R.id.switch1);

		searchEditText = (EditText) findViewById(R.id.searchText);
		image = (ImageView) findViewById(R.id.imageDisplay);
		go = (Button) findViewById(R.id.go);
		go.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// TODO Auto-generated method stub
				if (isConnectedOnline()) {
					counter = 0;
					new LoadImageUrls().execute(flickrUrl);
					next.setEnabled(true);
					previous.setEnabled(true);
				} else Toast.makeText(
				MainActivity.this,
				MainActivity.this.getResources().getString(
				R.string.no_internet), Toast.LENGTH_LONG)
					.show();
			}
		});

		next = (ImageView) findViewById(R.id.next);
		next.setEnabled(false);
		next.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (counter == (photoUrls.size())) {
					counter = 0;
				}
				setupImage(photoUrls.get(counter).getUrl());
				counter++;
				Log.d("demo", "Counter after Next " + counter);
			}
		});
		previous = (ImageView) findViewById(R.id.previous);
		previous.setEnabled(false);
		previous.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (counter == 1) {
					counter = 21;
				}
				counter--;
				setupImage(photoUrls.get(counter - 1).getUrl());
				Log.d("demo", "Counter after previous " + counter);
			}
		});

	}

	private boolean isConnectedOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public void setupImage(String imageUrl) {
		if (isConnectedOnline()) new LoadImage().execute(imageUrl);
		else Toast.makeText(
		MainActivity.this,
		MainActivity.this.getResources().getString(
		R.string.no_internet), Toast.LENGTH_LONG).show();
	}

	public class LoadImageUrls extends
	AsyncTask < String, Integer, ArrayList < Photo >> {

		String baseUrl = null;
		HashMap < String, String > params = new HashMap < String, String > ();

		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setCancelable(false);
			dialog.setMessage(MainActivity.this.getResources().getString(
			R.string.progress_message));
			dialog.show();
		}

		@Override
		protected ArrayList < Photo > doInBackground(String...params) {
			try {
				baseUrl = params[0];
				setupParams();
				URL url = new URL(getEncodedUrl());
				HttpURLConnection con = (HttpURLConnection) url.openConnection();

				con.setRequestMethod("GET");
				con.connect();
				int statusCode = con.getResponseCode();
				if (statusCode == HttpURLConnection.HTTP_OK) {
					InputStream in = con.getInputStream();
					if (parserSwitch.isChecked()) return PhotoUtil.PhotoSaxParser.parsePhotos( in );
					else return PhotoUtil.PhotoPullParser.parsePhotos( in );
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(ArrayList < Photo > result) {
			// TODO Auto-generated method stub
			dialog.dismiss();
			super.onPostExecute(result);
			photoUrls = result;
			setupImage(result.get(0).getUrl());
			counter++;
			Log.d("Demo", "Total number of images " + result.size());
		}

		public void setupParams() {

			String searchString = searchEditText.getText().toString();

			params.put("method", "flickr.photos.search");
			params.put("api_key", "enter your api key here");
			params.put("text", searchString);
			params.put("extras", "url_m");
			params.put("per_page", "20");
			params.put("format", "rest");
		}

		public String getEncodedUrl() {
			String encodedUrl = baseUrl + "?" + getEncodedParams();
			Log.d("demo", "Encoded URL" + encodedUrl);
			return encodedUrl;
		}

		public String getEncodedParams() {
			StringBuilder sb = new StringBuilder();
			for (String key: params.keySet()) {
				try {
					String value = URLEncoder.encode(params.get(key), "UTF-8");
					if (sb.length() > 0) {
						sb.append("&");
					}
					sb.append(key + "=" + value);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			return sb.toString();
		}
	}

	public class LoadImage extends AsyncTask < String, Integer, Bitmap > {

		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = new ProgressDialog(MainActivity.this);
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setCancelable(false);
			dialog.setMessage(MainActivity.this.getResources().getString(
			R.string.progress_message));
			dialog.show();
		}

		@Override
		protected Bitmap doInBackground(String...params) {
			AndroidHttpClient client = AndroidHttpClient.newInstance("Android Agent");
			InputStream in = null;
			HttpGet request = new HttpGet(params[0]);
			try {
				HttpResponse response = client.execute(request); in = response.getEntity().getContent();

				Bitmap image = BitmapFactory.decodeStream( in );
				return image;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if ( in != null) {
					try { in .close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			image.setImageBitmap(result);
			dialog.dismiss();
		}

	}
}