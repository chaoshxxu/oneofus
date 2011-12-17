package isun.oneofus;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class Solver {
	
	public String username = "Isun";
	public String password = "*****************";
	
	public int h;
	public int w;
	public String map;
	public int unit;
	public int level;
	public Node node[];
	public int total, x;
	public int dup[] = new int[500];
	public int idx;
	public int nextX;
	public int ccnt[] = new int[11000];
	public int dcnt[] = new int[11000];
	public boolean fail;

	
	public void getMap(){
		String tLine = "";
		HttpClient httpClient = new HttpClient();
		GetMethod getMethod = new GetMethod("http://www.hacker.org/oneofus/index.php?name=" + username + "&password=" + password);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if(statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: "+getMethod.getStatusLine());
			}
			byte[] responseBody = getMethod.getResponseBody();
			tLine = new String(responseBody, "utf-8");
		}
		catch(Exception e) {
			getMethod.releaseConnection();
		}
		Pattern p = Pattern.compile("FlashVars=\"x=(\\d*?)&y=(\\d*?)&board=([\\s\\S]*?)\"[\\s\\S]+size=\"3\" value=\"(\\d+)\"");
		Matcher m = p.matcher(tLine);
		if (m.find()){
			w = Integer.parseInt(m.group(1));
			h = Integer.parseInt(m.group(2));
			map = m.group(3);
			level = Integer.parseInt(m.group(4));
			unit = map.length() / w / h;
		}
		System.out.println("w   = " + w);
		System.out.println("h   = " + h);
//		System.out.println("map = " + map);
	}
	
	boolean same(int a, int b){
		String ca = map.substring(a * unit, a * unit + unit / 2);
		String cb = map.substring(b * unit, b * unit + unit / 2);
		String sa = map.substring(a * unit + unit / 2, a * unit + unit);
		String sb = map.substring(b * unit + unit / 2, b * unit + unit);
		return ca.equals(cb) || sa.equals(sb);
	}

	public void solve(){
		
		node = new Node[w*h];
		for (int i = 0; i < node.length; i++) {
			node[i] = new Node();
		}
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				for (int i = 0; i < h; i++){
					if (i != y && same(y*w+x, i*w+x)){
						node[y * w + x].neighbour.add(i*w+x);
					}
				}
				for (int i = 0; i < w; i++){
					if (i != x && same(y*w+x, y*w+i)){
						node[y * w + x].neighbour.add(y*w+i);
					}
				}
			}
		}
/*		for (int i = 0; i < node.length; i++) {
			System.out.print(i + ": ");
			for (int j = 0; j < node[i].neighbour.size(); j++) {
				System.out.print(node[i].neighbour.get(j) + " ");
			}
			System.out.println();
		}
*/
		int minSizeI = 0;
		for (int i = 0; i < node.length; i++){
			if (node[i].neighbour.size() < node[minSizeI].neighbour.size()){
				minSizeI = i;
			}
		}

		
		x = minSizeI;
		long begin = System.currentTimeMillis();
		do {
//			System.out.println("Level: " + level);
			for (int i = 0; i < node.length; i++){
				node[i].in = node[i].out = -1;
			}
			for (int i = 0; i < ccnt.length; i++){
				ccnt[i] = 0;
			}
			for (int i = 0; i < dcnt.length; i++){
				dcnt[i] = 0;
			}
			for (int i = 0; i < dup.length; i++){
				dup[i] = -1;
			}
			idx = 0;
			total = 1;
			fail = false;
			while (total < w*h){
				find();
				if (fail){
					break;
				}
			}
			x = nextX;
		} while (total < w*h);
		System.out.println("Level: " + level);
		System.out.println("Solving Time: " + (System.currentTimeMillis() - begin) + "ms");
	}
	
	void find(){
		System.out.println(x + " " + node[x].neighbour.size() + " " + node[x].in + " -- " + total + " : " + ccnt[x]);
		if (dup[idx] >= 0){
			ccnt[dup[idx]]--;
		}
		dup[idx] = x;
		ccnt[x]++;
		idx = (idx + 1) % dup.length;
		if (ccnt[x] > dup.length / 2 - 1){
			fail = true;
			nextX = x;
			return;
		}
		
		
/*		for (int j = 0; j < total; j++){
			System.out.print("\t");
		}
		System.out.println(x);
*/
		Node n = node[x];
		if (total == w*h){
			return;
		}
		int i, next, fewNeigb = -1;
		int begin = (int) (System.currentTimeMillis() % n.neighbour.size());
		for (i = begin; i < n.neighbour.size() + begin; i++){
			next = n.neighbour.get(i%n.neighbour.size());
			if (node[next].in >= 0 || node[next].out >= 0){
				continue;
			}
			if (fewNeigb < 0 || node[next].neighbour.size() < node[fewNeigb].neighbour.size()){
				fewNeigb = next;
			}
		}
		if (fewNeigb >= 0){
			n.out = fewNeigb;
			node[fewNeigb].in = x;
			total++;
			x = fewNeigb;
			return;
		}
		
/*		do {
			next = n.neighbour.get((int) (System.currentTimeMillis() % n.neighbour.size()));
		} while (n.in == next);
*/
		int minn = 2000000000, I = -1;
		for (i = 0; i < n.neighbour.size(); i++){
			next = n.neighbour.get(i);
			if (n.in == next)continue;
			if (dcnt[next] < minn){
				minn = dcnt[next];
				I = i;
			}
		}
		if (I < 0){
			fail = true;
			return;
		}
		next = n.neighbour.get(I);
		dcnt[next]++;

		
		n.out = next;
//		System.out.println("x :" + x);
//		System.out.println("next :" + next);
		if (node[next].in < 0){
			node[next].in = x;
//			System.out.println("circle!");
			long goTimes = System.currentTimeMillis() % total;
			int curNext = next;
			while (goTimes-- > 0){
				curNext = node[curNext].out;
			}
			node[node[curNext].out].in = -1;
			node[curNext].out = -1;
			x = curNext;
		} else {
			int cur = x, p;
//			System.out.println("n.in :" + n.in);
			int cnt = 0;
			while (true) {
//				System.out.println("cur :" + cur);
				p = node[cur].in;
				cnt++;
				node[cur].in ^= node[cur].out;
				node[cur].out ^= node[cur].in;
				node[cur].in ^= node[cur].out;
				if (p == next){
					break;
				}
				cur = p;
			}
//			System.out.println("----");
			node[next].out = x;
			node[cur].out = -1;
			x = cur;
/*			if (cnt <= 3){
				int tmp = 5;
				while (node[x].in >= 0 && --tmp > 0){
					p = node[x].in;
					node[x].in = -1;
					x = p;
					node[x].out = -1;
					total--;
				}
			}
*/			
		}
	}

	
	public void submit(){
		int cur;
		for (cur = 0; cur < w*h; cur++){
			if (node[cur].in < 0){
				break;
			}
		}
		String result = "";
		do {
//			System.out.print((cur / w) + "," + (cur % w) + " ");
			result += (result.isEmpty()?"":"_") + (Integer.toHexString(cur % w)) + "," + (Integer.toHexString(cur / w));
			cur = node[cur].out;
		} while (cur >= 0);

		HttpClient httpClient = new HttpClient();
		PostMethod postMethod = new PostMethod("http://www.hacker.org/oneofus/index.php");
		postMethod.addParameter("name", username);
		postMethod.addParameter("password", password);
		postMethod.addParameter("path", result);
		postMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		httpClient.getParams().setContentCharset("UTF-8"); 
//		System.out.println("\nhttp://www.hacker.org/oneofus/index.php?name=" + username + "&password=" + password + "&path=" + result);
		boolean success;
		do {
			success = false;
			try {
				httpClient.executeMethod(postMethod);
				success = true;
			} catch (Exception e) {
			}
		} while (!success);
	}

	public static void main(String[] args){
		
		Solver solver = new Solver();
		while (true){
			solver.getMap();
			solver.solve();
			solver.submit();
		}
	}
	
	class Node{
		List<Integer> neighbour;
		int in;
		int out;
		
		Node() {
			neighbour = new ArrayList<Integer>();
			in = out = -1;
		}
	}

}
