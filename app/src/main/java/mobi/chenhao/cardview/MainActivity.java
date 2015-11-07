package mobi.chenhao.cardview;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements View.OnClickListener{

    private CardView cardView;
    private List<String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cardView=(CardView)findViewById(R.id.card_view);
        findViewById(R.id.card_view_back).setOnClickListener(this);
        CardAdapter cardAdapter=new CardAdapter();
        cardView.setAdapter(cardAdapter,data.size());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.card_view_back:
                cardView.showLast();
                break;
        }
    }

    private class CardAdapter extends BaseAdapter{

        public CardAdapter() {
            data=new ArrayList<>();
            data.add("#419BF9");
            data.add("#63CE69");
            data.add("#FD9933");
            data.add("#ff6464");
            data.add("#CD4F39");
            data.add("#555555");
            data.add("#20d9ab");
            data.add("#63ce69");
            data.add("#9e6930");
            data.add("#7f7070");
            data.add("#f85b5b");
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Object getItem(int i) {
            return data.get(i%data.size());
        }

        @Override
        public long getItemId(int i) {
            return i%data.size();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int postion=i%data.size();
            TextView t=new TextView(MainActivity.this);
            t.setBackgroundColor(Color.parseColor(data.get(postion)));
            t.setText("Index:"+postion+"-Color:"+data.get(postion));
            t.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            t.setGravity(Gravity.CENTER);
            t.setTextColor(Color.WHITE);
            return t;
        }
    }


}
