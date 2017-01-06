package com.example.ankit.stock2;

        import android.graphics.Color;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.CheckBox;
        import android.widget.CompoundButton;
        import android.widget.EditText;
        import android.widget.LinearLayout;
        import android.widget.SeekBar;
        import android.widget.TextView;
        import android.widget.ToggleButton;

        import com.firebase.client.DataSnapshot;
        import com.firebase.client.Firebase;
        import com.firebase.client.FirebaseError;
        import com.firebase.client.ValueEventListener;
        import com.jjoe64.graphview.GraphView;
        import com.jjoe64.graphview.series.DataPoint;
        import com.jjoe64.graphview.series.LineGraphSeries;

        import java.util.ArrayList;
        import java.util.List;

public class MainActivity extends AppCompatActivity {

    int delay = 1;
    int counter;
    double sum;
    int averageDelay = 0;

    LineGraphSeries<DataPoint> closeddataseries;
    LineGraphSeries<DataPoint> opendataseries;
    LineGraphSeries<DataPoint> volumeddataseries;
    LineGraphSeries<DataPoint> closedaverageseries;
    LineGraphSeries<DataPoint> openaverageseries;
    LineGraphSeries<DataPoint> volumeaverageseries;

    GraphView graph;

    CheckBox closedcb;
    CheckBox opencb;
    CheckBox volumecb;

    ToggleButton tb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        closedcb = (CheckBox)findViewById(R.id.closed);
        opencb = (CheckBox)findViewById(R.id.open);
        volumecb = (CheckBox)findViewById(R.id.volume);

        tb = (ToggleButton)findViewById(R.id.toggleButton2);

        Firebase.setAndroidContext(this);
        final Firebase myFirebaseRef = new Firebase("https://stocktest.firebaseio.com");

        final TextView textView = (TextView)findViewById(R.id.textview);
        final EditText dataTextBox = (EditText)findViewById(R.id.dataBox);
        Button button = (Button)findViewById(R.id.dataButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                List<Integer> possibleDelays = new ArrayList<Integer>();
                possibleDelays.add(1);
                possibleDelays.add(2);
                possibleDelays.add(3);
                possibleDelays.add(5);
                possibleDelays.add(10);
                possibleDelays.add(15);
                possibleDelays.add(30);
                TextView tv = (TextView) findViewById(R.id.dataIncorrectText);
                int userInput = Integer.parseInt(dataTextBox.getText().toString());
                if (possibleDelays.contains(userInput)) {
                    delay = userInput;
                    tv.setText("Delay set");
                } else {
                    tv.setText("Not a possible delay");
                }
            }
        });
        SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText("Change delay for average: " + progress);
                averageDelay = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        graph.setTitle("Stock Visualization");
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Closed Value");

        counter = 0;
        sum = 0;

        closeddataseries = new LineGraphSeries<DataPoint>();
        closedaverageseries = new LineGraphSeries<DataPoint>();
        closedaverageseries.setColor(Color.RED);
        opendataseries = new LineGraphSeries<DataPoint>();
        opendataseries.setColor(Color.YELLOW);
        openaverageseries = new LineGraphSeries<DataPoint>();
        openaverageseries.setColor(Color.MAGENTA);
        volumeddataseries = new LineGraphSeries<DataPoint>();
        volumeddataseries.setColor(Color.GREEN);
        volumeaverageseries = new LineGraphSeries<DataPoint>();
        volumeaverageseries.setColor(Color.CYAN);

        Log.d("delay", "delay = " + delay + "");

        myFirebaseRef.child("0").child("data").child("" + delay).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d("peep", "data changed");
                addDatapointsToSeries(snapshot, closeddataseries, closedaverageseries, "c");
                addDatapointsToSeries(snapshot, opendataseries, openaverageseries, "o");
                addDatapointsToSeries(snapshot, volumeddataseries, volumeaverageseries, "v");
                counter += delay;
                if (closedcb.isChecked()) {
                    graph.removeSeries(closeddataseries);
                    Log.d("peep", "graphing closed data");
                    graph.addSeries(closeddataseries);
                    graph.removeSeries(opendataseries);
                    graph.removeSeries(volumeddataseries);
                } else if (opencb.isChecked()) {
                    graph.removeSeries(opendataseries);
                    graph.addSeries(opendataseries);
                    graph.removeSeries(closeddataseries);
                    graph.removeSeries(volumeddataseries);
                } else if (volumecb.isChecked()) {
                    graph.removeSeries(volumeddataseries);
                    graph.addSeries(volumeddataseries);
                    graph.removeSeries(closeddataseries);
                    graph.removeSeries(opendataseries);
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }

    public void addDatapointsToSeries(DataSnapshot snapshot, LineGraphSeries<DataPoint> dataseries, LineGraphSeries<DataPoint> averageseries, String datatype)
    {
        String valuec = snapshot.child(counter + "").child(datatype).getValue().toString();
        double num = counter;
        if (!valuec.equals("")) {
            DataPoint dp = new DataPoint(counter, Double.parseDouble(valuec));
            dataseries.appendData(dp, false, counter + 10);
            sum += Double.parseDouble(valuec);
            if (counter % (averageDelay + 1) == 0) {
                DataPoint averagepoint;
                averagepoint = new DataPoint(counter, sum / (num + 1));
                averageseries.appendData(averagepoint, false, counter + 10);
            }
        }
    }

    public void setToggleButtonListener(ToggleButton tb, final LineGraphSeries<DataPoint> averageseries)
    {
        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    graph.removeSeries(averageseries);
                } else {
                    graph.addSeries(averageseries);
                }
            }
        });
    }

    public void onCheckboxClicked(View view) {
        String text = "";
        if(view.getId() == R.id.closed) {
            text = "Closed";
            tb.setTextOn("Show " + text + " Average");
            tb.setTextOff("Hide " + text + " Average");
            tb.setChecked(tb.isChecked());
            setToggleButtonListener(tb, closedaverageseries);
            while(opencb.isChecked())
                opencb.toggle();
            while(volumecb.isChecked())
                volumecb.toggle();
            graph.removeSeries(closeddataseries);
            graph.addSeries(closeddataseries);
            graph.removeSeries(opendataseries);
            graph.removeSeries(volumeddataseries);
        }
        else if(view.getId() == R.id.open)
        {
            text = "Open";
            tb.setTextOn("Show " + text + " Average");
            tb.setTextOff("Hide " + text + " Average");
            tb.setChecked(tb.isChecked());
            setToggleButtonListener(tb, openaverageseries);
            while(closedcb.isChecked())
                closedcb.toggle();
            while(volumecb.isChecked())
                volumecb.toggle();
            graph.removeSeries(opendataseries);
            graph.addSeries(opendataseries);
            graph.removeSeries(closeddataseries);
            graph.removeSeries(volumeddataseries);
        }
        else if(view.getId() == R.id.volume)
        {
            text = "Volume";
            tb.setTextOn("Show " + text + " Average");
            tb.setTextOff("Hide " + text + " Average");
            tb.setChecked(tb.isChecked());
            setToggleButtonListener(tb, volumeaverageseries);
            while(closedcb.isChecked())
                closedcb.toggle();
            while(opencb.isChecked())
                opencb.toggle();
            graph.removeSeries(volumeddataseries);
            graph.addSeries(volumeddataseries);
            graph.removeSeries(closeddataseries);
            graph.removeSeries(opendataseries);
        }
    }
}
