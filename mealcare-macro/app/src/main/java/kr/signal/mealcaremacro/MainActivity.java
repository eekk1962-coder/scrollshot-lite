package kr.signal.mealcaremacro;
import android.app.*;import android.os.*;import android.content.*;import android.content.pm.*;import android.graphics.Color;import android.provider.Settings;import android.view.*;import android.widget.*;import java.time.*;import java.util.*;
public class MainActivity extends Activity {
 LocalDate date=LocalDate.now(); TextView dateView,status,log; CheckBox preview; final Handler h=new Handler(); 
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
 TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(0xff20252a);t.setPadding(0,dp(6),0,dp(6));return t;}
 Button button(String s,Runnable r){Button b=new Button(this);b.setAllCaps(false);b.setText(s);b.setOnClickListener(v->r.run());return b;}
 @Override public void onCreate(Bundle b){super.onCreate(b);try{date=LocalDate.parse(getPreferences(0).getString("date",date.toString()));}catch(Exception ignored){}
 ScrollView sc=new ScrollView(this);LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(24),dp(26),dp(24),dp(24));sc.addView(l);setContentView(sc);
 l.addView(text("헬시팩 예약",28));l.addView(text("석식 · 헬시팩 · 날짜별 1개",15));l.addView(text("시작 날짜 (포함)",14));
 dateView=text(date.toString(),22);l.addView(dateView);l.addView(button("날짜 선택",()->new DatePickerDialog(this,(v,y,m,d)->{date=LocalDate.of(y,m+1,d);dateView.setText(date.toString());getPreferences(0).edit().putString("date",date.toString()).apply();},date.getYear(),date.getMonthValue()-1,date.getDayOfMonth()).show()));
 preview=new CheckBox(this);preview.setText("예약하지 않고 탐색만 하기");l.addView(preview);
 l.addView(button("접근성 권한 설정",()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))));
 l.addView(button("시작",()->startMacro()));l.addView(button("중지",()->{if(MacroService.instance!=null)MacroService.instance.stop("사용자 중지");}));
 status=text("",15);l.addView(status);l.addView(text("실행 기록",19));log=text("",13);l.addView(log);
 l.addView(button("기록 공유",()->{Intent i=new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,getSharedPreferences("run",0).getString("log",""));startActivity(Intent.createChooser(i,"실행 기록"));}));
 l.addView(button("확인된 예약 날짜 기록 초기화",()->new AlertDialog.Builder(this).setMessage("앱 내부의 중복 방지 기록만 지웁니다. 실제 예약은 취소되지 않습니다.").setNegativeButton("취소",null).setPositiveButton("초기화",(d,w)->{if(MacroService.instance==null||!MacroService.instance.running)getSharedPreferences("run",0).edit().remove("booked").apply();}).show()));
 }
 void startMacro(){if(MacroService.instance==null){status.setText("접근성 설정에서 밀케어 예약 자동화를 켜주세요.");return;} if(MacroService.instance.running)return;
 Intent query=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);ArrayList<ResolveInfo> matches=new ArrayList<>();
 for(ResolveInfo ri:getPackageManager().queryIntentActivities(query,0)){String label=ri.loadLabel(getPackageManager()).toString().replaceAll("\\s+","").toLowerCase(Locale.ROOT);if((label.equals("mealcare")||label.equals("밀케어")||label.equals("밀케어앱"))&&!ri.activityInfo.packageName.equals(getPackageName()))matches.add(ri);}
 if(matches.size()!=1){status.setText("밀케어 앱을 정확히 찾지 못했습니다. 앱 설치와 이름을 확인하세요.");return;}
 ResolveInfo ri=matches.get(0);Intent launch=getPackageManager().getLaunchIntentForPackage(ri.activityInfo.packageName);if(launch==null){status.setText("밀케어 실행 불가");return;}
 new AlertDialog.Builder(this).setTitle(preview.isChecked()?"탐색 시작":"예약 시작").setMessage(date+" 포함 이후, 현재 표시되는 예약 가능 헬시팩을 날짜별 1개씩 "+(preview.isChecked()?"탐색합니다.":"예약합니다.")).setNegativeButton("취소",null).setPositiveButton("시작",(d,w)->{MacroService.instance.begin(ri.activityInfo.packageName,date,preview.isChecked());startActivity(launch);}).show();
 }
 final Runnable refresh=new Runnable(){public void run(){SharedPreferences p=getSharedPreferences("run",0);status.setText((MacroService.instance==null?"접근성 꺼짐":"접근성 연결됨")+" · "+p.getString("status","대기"));log.setText(p.getString("log",""));h.postDelayed(this,800);}};
 @Override protected void onResume(){super.onResume();h.removeCallbacks(refresh);h.post(refresh);}
 @Override protected void onPause(){h.removeCallbacks(refresh);super.onPause();}
}