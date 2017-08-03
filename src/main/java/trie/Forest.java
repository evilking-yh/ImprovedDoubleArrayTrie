package trie;

import java.util.ArrayList;
import java.util.List;

public class Forest implements WoodInterface {
	private WoodInterface[] chars = new WoodInterface[65536];
	
	private boolean status = false;

	public WoodInterface add(WoodInterface branch) {
		WoodInterface temp = this.chars[branch.getC()];
		if (temp == null)
			this.chars[branch.getC()] = branch;
		return this.chars[branch.getC()];
	}

	public boolean contains(char c) {
		return this.chars[c] != null;
	}

	public WoodInterface get(char c) {
		if (c > 66535) {
//			System.out.println(c);
			return null;
		}
		return this.chars[c];
	}


	public int compareTo(char c) {
		return 0;
	}

	public boolean equals(char c) {
		return false;
	}

	public char getC() {
		return '\000';
	}

	public void setStatus(int status) {
	}

	public int getSize() {
		return this.chars.length;
	}

	public String[] getParams() {
		return null;
	}

	public void setParam(String[] param) {
	}

	/**
	 * 清空树释放内存
	 */
	public void clear(){
		chars = new WoodInterface[65535] ;
	}

	public int getChildCount() {
		return this.chars.length;
	}

	public boolean getStatus() {
		return this.status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public WoodInterface[] getBranchs() {
		//过滤掉null子节点
		List<WoodInterface> tmpList = new ArrayList<WoodInterface>();
		for(WoodInterface tmp: chars){
			if(tmp != null){
				tmpList.add(tmp);
			}
		}
		WoodInterface[] chars2 = new WoodInterface[tmpList.size()];
		for(int i = 0;i < tmpList.size(); i++){
			chars2[i] = tmpList.get(i);
		}
		return chars2;
	}
}