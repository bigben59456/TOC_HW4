/*###################
姓名 : 吳邦宇

學號 : F74006292

簡述 : 
將json檔 "土地區段" 欄位取出若符合要找的格式且要未存過的資訊
搜尋整個檔案中相同的路段最高最低價
並判斷交易時間是否重複，若否則增加交易次數
存下此資訊後繼續
最後印出 交易次數=最大交易次數 的那幾筆資料

參考 : 檔案的讀取 http://stackoverflow.com/questions/4308554/simplest-way-to-read-json-from-a-url-in-java
###################*/
import java.io.*;
import java.net.*;
import org.json.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.Charset;

public class TocHw4
{
	public static void main(String[] args) throws IOException, JSONException
	{
		JSONArray json = readJsonFromUrl(args[0]); //web url = args[0]
		
		ArrayList<String> road=new ArrayList<String>(); //the road 
		ArrayList<Integer> high_price=new ArrayList<Integer>(); //high price of the road
		ArrayList<Integer> low_price=new ArrayList<Integer>(); //low price of the road
		ArrayList<Integer> times=new ArrayList<Integer>(); //transaction times of the road
			
		for(int i=0 ;i<json.length() ;++i)
		{
			String road_detail=json.getJSONObject(i).getString("土地區段位置或建物區門牌"); //road in detail of object(i) in array (json.getJSONObject(i) is something like <C style array : json[i]>)
			String use_road=new String(); //road without detail
			ArrayList<Integer> tmp_index=new ArrayList<Integer>(); //how to cut use_road
			
			if(road_detail.lastIndexOf("大道")!=-1) tmp_index.add(road_detail.lastIndexOf("大道")+2); //match 大道 (use lastIndexOf to avoid case like "高雄市路竹區延平路756巷54弄1~30號" the first "路")
			if(road_detail.lastIndexOf("路")!=-1) tmp_index.add(road_detail.lastIndexOf("路")+1); //match 路 (use lastIndexOf to avoid case like "高雄市路竹區延平路756巷54弄1~30號" the first "路")
			if(road_detail.lastIndexOf("街")!=-1) tmp_index.add(road_detail.lastIndexOf("街")+1); //match 街 (use lastIndexOf to avoid case like "高雄市路竹區延平路756巷54弄1~30號" the first "路")
			if(road_detail.lastIndexOf("巷")!=-1 && tmp_index.size()==0) tmp_index.add(road_detail.lastIndexOf("巷")+1); //match 巷 && no match "路 大道 街"(use lastIndexOf to avoid case like "高雄市路竹區延平路756巷54弄1~30號" the first "路")

			if(tmp_index.size()==0) continue; //nothing match -> it's not a road

			use_road=road_detail.substring(0 ,Collections.max(tmp_index)); //cut the detail
			if(road.contains(use_road)) continue; //has set in array list

			road.add(use_road); //set this road in array list
			high_price.add(json.getJSONObject(i).getInt("總價元")); //the index of this road high price
			low_price.add(json.getJSONObject(i).getInt("總價元")); //the index of low price if this road
			times.add(0); //add index of this road has how many transaction

			ArrayList<Integer> date=new ArrayList<Integer>(); //save when has transaction
			Pattern pattern=Pattern.compile(use_road); //find this road name

			for(int j=i ;j<json.length() ;++j) //if run this loop means first time match this road so can start at i
			{
				Matcher matcher=pattern.matcher(json.getJSONObject(j).getString("土地區段位置或建物區門牌")); //to match this pattern with object(j) in array
				
				if(!matcher.find()) continue; //can't find the road contain this road name
				if(matcher.start()==0) //this road is we are searching
				{
					int when=json.getJSONObject(j).getInt("交易年月"); //transaction time
					/*price*/
					if(json.getJSONObject(j).getInt("總價元")>high_price.get(road.indexOf(use_road))) high_price.set(road.indexOf(use_road) ,json.getJSONObject(j).getInt("總價元")); //update high_price
					if(json.getJSONObject(j).getInt("總價元")<low_price.get(road.indexOf(use_road))) low_price.set(road.indexOf(use_road) ,json.getJSONObject(j).getInt("總價元")); //update low_price
						
					/*date*/
					if(date.contains(when)) continue; //this transaction has counted
					else //didn't been counted
					{
						int tmp=times.get(road.indexOf(use_road)); //get times
						times.set(road.indexOf(use_road) ,tmp+1); //times+1 and save
						date.add(when); //this date has transaction	
					}
				}
			}
		}
		for(int i=0 ;i<road.size() ;++i)
			if(times.get(i)==Collections.max(times)) //has most times of transaction
				System.out.println(road.get(i)+", 最高成交價: "+high_price.get(i)+", 最低成交價: "+low_price.get(i));
	}
				
	private static String readAll(Reader rd) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		int cp;
		while((cp=rd.read())!=-1) sb.append((char)cp);
		return sb.toString();
	}

	public static JSONArray readJsonFromUrl(String url) throws IOException, JSONException
	{
		InputStream is = new URL(url).openStream();
		try
		{
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONArray jsonRealPrice = new JSONArray(jsonText);
			return jsonRealPrice;
		}
		finally
		{
			is.close();
		}
	}
}
