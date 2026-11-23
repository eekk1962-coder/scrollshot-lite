package com.reelreply.assistant;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class MainActivity extends Activity {
  private static final int PICK=10; private static final int BLUE=0xff3182f6, BLACK=0xff191f28, GRAY=0xff6b7684;
  private LinearLayout root, results; private ImageView preview; private EditText context, endpoint, token; private TextView status;
  private Uri selected; private final ArrayList<String> drafts=new ArrayList<>();
  private final android.os.Handler handler=new android.os.Handler(android.os.Looper.getMainLooper());
  int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}
  TextView text(String s,int sp,int color){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(sp);return t;}
  LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  void add(LinearLayout parent,View view,int margin){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(margin);parent.addView(view,p);}
  @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(0xfff9fafb);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(0xfff9fafb);root=column();root.setPadding(dp(22),dp(30),dp(22),dp(30));scroll.addView(root);setContentView(scroll);
    TextView title=text("릴답",28,BLACK);title.setTypeface(null,1);add(root,title,5);
    add(root,text("릴스를 보고, 딱 맞는 한마디",15,GRAY),24);
    Button choose=button("릴스 캡처 선택",BLUE);add(root,choose,16);choose.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_GET_CONTENT);i.setType("image/*");startActivityForResult(i,PICK);});
    preview=new ImageView(this); preview.setAdjustViewBounds(true);preview.setMaxHeight(dp(250));preview.setScaleType(ImageView.ScaleType.FIT_CENTER);add(root,preview,12);
    add(root,text("영상 설명 · 캡션 (선택)",14,BLACK),7);
    context=new EditText(this);context.setSingleLine(false);context.setMinLines(2);context.setTextSize(14);context.setHint("캡처만으로 알 수 없는 장면, 대사, 분위기를 적어주세요");add(root,context,18);
    Button generate=button("댓글 3개 만들기",BLUE);add(root,generate,10);generate.setOnClickListener(v->generate());
    status=text("영상의 전체 맥락이 불명확하면 댓글을 생성하지 않습니다.",13,GRAY);add(root,status,15);
    results=column();add(root,results,20);
    TextView settings=text("AI 연결 설정 (선택)",16,BLACK);settings.setTypeface(null,1);add(root,settings,8);
    add(root,text("기본으로는 로컬 예시를 사용합니다. 실제 이미지 AI 분석은 본인 소유의 HTTPS AI 프록시를 연결하세요. API 키를 앱에 직접 넣지 않습니다.",12,GRAY),7);
    endpoint=new EditText(this);endpoint.setSingleLine(true);endpoint.setTextSize(13);endpoint.setHint("https://your-domain.example/analyze");add(root,endpoint,6);
    token=new EditText(this);token.setSingleLine(true);token.setTextSize(13);token.setHint("프록시 접근 토큰 (선택)");add(root,token,8);
    getPreferences(0).edit().apply();
    endpoint.setText(getPreferences(0).getString("endpoint",""));
    token.setText(getPreferences(0).getString("token",""));
    Button save=button("연결 설정 저장",0xfff2f4f6);save.setTextColor(BLACK);add(root,save,20);
    save.setOnClickListener(v->{getPreferences(0).edit().putString("endpoint",endpoint.getText().toString().trim()).putString("token",token.getText().toString().trim()).apply();Toast.makeText(this,"저장했어요",Toast.LENGTH_SHORT).show();});
    add(root,text("댓글은 자동 게시되지 않습니다. 선택한 문구를 복사한 뒤 Instagram에서 직접 확인하고 게시하세요.",12,GRAY),5);
    Intent incoming=getIntent();if(Intent.ACTION_SEND.equals(incoming.getAction()) && incoming.getType()!=null && incoming.getType().startsWith("image/")){Uri u=incoming.getParcelableExtra(Intent.EXTRA_STREAM);if(u!=null)load(u);}
  }
  Button button(String name,int color){Button b=new Button(this);b.setText(name);b.setTextAllCaps(false);b.setTextSize(15);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));b.setTextColor(-1);b.setMinHeight(dp(52));return b;}
  @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req==PICK && result==RESULT_OK && data!=null)load(data.getData());}
  void load(Uri u){selected=u;try{preview.setImageURI(u);status.setText("캡처를 선택했어요. 댓글 후보를 생성할 수 있습니다.");}catch(Exception e){status.setText("이미지를 열지 못했어요.");}}
  void generate(){if(selected==null){status.setText("릴스 캡처를 먼저 선택해 주세요.");return;}String url=endpoint.getText().toString().trim();if(!url.startsWith("https://")){showLocal();return;}status.setText("화면을 분석하고 있어요…");new Thread(()->remote(url,token.getText().toString().trim())).start();}
  void showLocal(){String caption=context.getText().toString().trim();results.removeAllViews();
    if(caption.length()<10){status.setText("이 이미지의 내용을 판단할 AI 연결이 없습니다. 정확한 댓글을 위해 HTTPS AI 프록시를 연결하거나 캡션을 자세히 입력해 주세요.");return;}
    status.setText("설명에 기반한 참고 문구입니다. 이미지 의미를 분석한 AI 결과는 아닙니다.");
    display(new String[]{"이 부분은 진짜 공감된다ㅋㅋ","저 상황이면 나도 저랬을 듯ㅋㅋ","보면서 괜히 웃게 되네ㅋㅋ"},false);
  }
  byte[] imageBytes() throws Exception {Bitmap b=MediaStore.Images.Media.getBitmap(getContentResolver(),selected);int w=b.getWidth(),h=b.getHeight();float scale=Math.min(1f,960f/Math.max(w,h));Bitmap small=Bitmap.createScaledBitmap(b,(int)(w*scale),(int)(h*scale),true);ByteArrayOutputStream out=new ByteArrayOutputStream();small.compress(Bitmap.CompressFormat.JPEG,75,out);if(small!=b)small.recycle();b.recycle();return out.toByteArray();}
  void remote(String address,String key){HttpURLConnection c=null;try{
    JSONObject body=new JSONObject();body.put("imageBase64",android.util.Base64.encodeToString(imageBytes(),android.util.Base64.NO_WRAP));body.put("caption",context.getText().toString());body.put("language","ko");body.put("mode","reels");
    c=(HttpURLConnection)new URL(address).openConnection();c.setRequestMethod("POST");c.setConnectTimeout(15000);c.setReadTimeout(45000);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");if(!key.isEmpty())c.setRequestProperty("Authorization","Bearer "+key);
    try(OutputStream o=c.getOutputStream()){o.write(body.toString().getBytes("UTF-8"));}
    int code=c.getResponseCode();if(code<200||code>=300)throw new IOException("HTTP "+code);
    StringBuilder response=new StringBuilder();try(BufferedReader reader=new BufferedReader(new InputStreamReader(c.getInputStream(),"UTF-8"))){String line;while((line=reader.readLine())!=null)response.append(line);}
    JSONObject result=new JSONObject(response.toString());boolean skip=result.optBoolean("skip",false);String reason=result.optString("reason","맥락이 불확실합니다.");JSONArray a=result.optJSONArray("comments");ArrayList<String> list=new ArrayList<>();if(a!=null)for(int i=0;i<Math.min(3,a.length());i++){String v=a.optString(i,"").trim();if(!v.isEmpty())list.add(v);}
    handler.post(()->{if(skip||list.isEmpty()){results.removeAllViews();status.setText("댓글 생성을 건너뛰었어요. "+reason);}else{status.setText("내용에 맞는 댓글 후보를 골랐어요.");display(list.toArray(new String[0]),true);}});
  }catch(Exception e){String msg=e.getMessage();handler.post(()->status.setText("AI 연결에 실패했어요: "+msg));}finally{if(c!=null)c.disconnect();}}
  void display(String[] suggestions,boolean ai){results.removeAllViews();for(int i=0;i<suggestions.length;i++){final String comment=suggestions[i];LinearLayout card=column();card.setPadding(dp(15),dp(13),dp(15),dp(13));card.setBackgroundColor(0xfff2f4f6);TextView v=text(comment,16,BLACK);add(card,v,6);Button copy=button("복사하기",BLUE);add(card,copy,0);copy.setOnClickListener(w->{((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("릴스 댓글",comment));Toast.makeText(this,"복사했어요",Toast.LENGTH_SHORT).show();});add(results,card,10);}}
}