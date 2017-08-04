/**
 * DoubleArrayTrie: Java implementation of Darts (Double-ARray Trie System)
 * 
 * <p>
 * Copyright(C) 2001-2007 Taku Kudo &lt;taku@chasen.org&gt;<br />
 * Copyright(C) 2009 MURAWAKI Yugo &lt;murawaki@nlp.kuee.kyoto-u.ac.jp&gt;
 * Copyright(C) 2012 KOMIYA Atsushi &lt;komiya.atsushi@gmail.com&gt;
 * </p>
 * 
 * <p>
 * The contents of this file may be used under the terms of either of the GNU
 * Lesser General Public License Version 2.1 or later (the "LGPL"), or the BSD
 * License (the "BSD").
 * </p>
 */
package dat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DoubleLinkedArrayTrie {
	private final static int BUF_SIZE = 16384;
	private final static int UNIT_SIZE = 8; // size of int + int

	private static class Node {
		int code;
		int depth;
		int left;
		int right;
	};

	private NodeIntBlock check;
	private NodeIntBlock base;
	private NodeBoolBlock used;
	
	private int size;
	private List<String> key;
	private int keySize;
	private int length[];
	private int value[];
	private int nextCheckPos;
	int error_;

	private int fetch(Node parent, List<Node> siblings) {
		if (error_ < 0)
			return 0;

		int prev = 0;

		for (int i = parent.left; i < parent.right; i++) {
			if ((length != null ? length[i] : key.get(i).length()) < parent.depth)
				continue;

			String tmp = key.get(i);	//第i行输入字符串

			int cur = 0;	//当前字符的unicode码+1，即下一个非空位
			if ((length != null ? length[i] : tmp.length()) != parent.depth)
				cur = (int) tmp.charAt(parent.depth) + 1;	//按层添加

			if (prev > cur) {
				error_ = -3;
				return 0;
			}

			if (cur != prev || siblings.size() == 0) {
				Node tmp_node = new Node();
				tmp_node.depth = parent.depth + 1;	//父节点的子节点
				tmp_node.code = cur;
				tmp_node.left = i;
				if (siblings.size() != 0)
					siblings.get(siblings.size() - 1).right = i;	//左兄弟节点的右边界更新为当前节点的左边界

				siblings.add(tmp_node);
			}

			prev = cur;
		}

		if (siblings.size() != 0)
			siblings.get(siblings.size() - 1).right = parent.right;

		return siblings.size();
	}

	private int insert(List<Node> siblings) {
		if (error_ < 0)
			return 0;

		int begin = 0;
		int pos = ((siblings.get(0).code + 1 > nextCheckPos) ? siblings.get(0).code + 1
				: nextCheckPos) - 1;
		int nonzero_num = 0;
		int first = 0;

		outer: while (true) {
			pos++;

			if(check.findValue(pos) != 0){
				nonzero_num++;
				continue;
			} else if (first == 0) {
				nextCheckPos = pos;
				first = 1;
			}

			begin = pos - siblings.get(0).code;
			if(used.findValue(begin))
				continue;

			for (int i = 1; i < siblings.size(); i++)
				if(check.findValue(begin + siblings.get(i).code) != 0)
					continue outer;

			break;
		}

		// -- Simple heuristics --
		// if the percentage of non-empty contents in check between the
		// index
		// 'next_check_pos' and 'check' is greater than some constant value
		// (e.g. 0.9),
		// new 'next_check_pos' index is written by 'check'.
		if (1.0 * nonzero_num / (pos - nextCheckPos + 1) >= 0.95)
			nextCheckPos = pos;

		used.setValue(begin, true);
		size = (size > begin + siblings.get(siblings.size() - 1).code + 1) ? size
				: begin + siblings.get(siblings.size() - 1).code + 1;

		for (int i = 0; i < siblings.size(); i++){
			check.setValue(begin + siblings.get(i).code, begin);
		}
		for (int i = 0; i < siblings.size(); i++) {
			List<Node> new_siblings = new ArrayList<Node>();

			if (fetch(siblings.get(i), new_siblings) == 0) {
				base.setValue(begin + siblings.get(i).code, (value != null) ? (-value[siblings.get(i).left] - 1) : (-siblings.get(i).left - 1));
				
				if (value != null && (-value[siblings.get(i).left] - 1) >= 0) {
					error_ = -2;
					return 0;
				}
			} else {
				int h = insert(new_siblings);
				base.setValue(begin + siblings.get(i).code, h);
			}
		}
		return begin;
	}

	public DoubleLinkedArrayTrie() {
		check = new NodeIntBlock();
		base = new NodeIntBlock();
		used = new NodeBoolBlock();
		size = 0;
		error_ = 0;
	}

	void clear() {
		check = new NodeIntBlock();
		base = new NodeIntBlock();
		used = new NodeBoolBlock();
		size = 0;
	}

	public int getUnitSize() {
		return UNIT_SIZE;
	}

	public int getSize() {
		return size;
	}

	public int getTotalSize() {
		return size * UNIT_SIZE;
	}

	public int getNonzeroSize() {
		int result = 0;
		for (int i = 0; i < size; i++)
			if(check.findValue(i) != 0)
				result++;
		return result;
	}

	public int build(List<String> key) {
		return build(key, null, null, key.size());
	}

	public int build(List<String> _key, int _length[], int _value[],
			int _keySize) {
		if (_keySize > _key.size() || _key == null)
			return 0;

		// progress_func_ = progress_func;
		key = _key;
		length = _length;
		keySize = _keySize;
		value = _value;
		
		base.setValue(0, 1);
		nextCheckPos = 0;

		Node root_node = new Node();
		root_node.left = 0;
		root_node.right = keySize;
		root_node.depth = 0;

		List<Node> siblings = new ArrayList<Node>();
		fetch(root_node, siblings);
		insert(siblings);

		used = null;
		key = null;

		return error_;
	}

	public void open(String fileName) throws IOException {
		File file = new File(fileName);
		size = (int) file.length() / UNIT_SIZE;
		check = new NodeIntBlock();
		base = new NodeIntBlock();

		DataInputStream is = null;
		try {
			is = new DataInputStream(new BufferedInputStream(
					new FileInputStream(file), BUF_SIZE));
			for (int i = 0; i < size; i++) {
				base.setValue(i, is.readInt());
				check.setValue(i, is.readInt());
			}
		} finally {
			if (is != null)
				is.close();
		}
	}

	public void save(String fileName) throws IOException {
		DataOutputStream out = null;
		try {
			out = new DataOutputStream(new BufferedOutputStream(
					new FileOutputStream(fileName)));
			for (int i = 0; i < size; i++) {
				out.writeInt(base.findValue(i));
				out.writeInt(check.findValue(i));
			}
			out.close();
		} finally {
			if (out != null)
				out.close();
		}
	}

	public int exactMatchSearch(String key) {
		return exactMatchSearch(key, 0, 0, 0);
	}

	public int exactMatchSearch(String key, int pos, int len, int nodePos) {
		if (len <= 0)
			len = key.length();
		if (nodePos <= 0)
			nodePos = 0;

		int result = -1;

		char[] keyChars = key.toCharArray();

		int b = base.findValue(nodePos);	//起始位置
		int p;

		for (int i = pos; i < len; i++) {	//从最开始的字符，依次状态转换到最后
			p = b + (int) (keyChars[i]) + 1;
			if(b == check.findValue(p)){
				b = base.findValue(p);
			}else{
				return result;	//中途状态转移不成功，退出
			}
		}

		p = b;	//转换成功
		int n = base.findValue(p);
		if(b == check.findValue(p) && n < 0){
			result = -n - 1;	//字符索引
		}
		return result;
	}
	
	public List<Integer> commonPrefixSearch(String key) {
		return commonPrefixSearch(key, 0, 0, 0);
	}

	public List<Integer> commonPrefixSearch(String key, int pos, int len,
			int nodePos) {
		if (len <= 0)
			len = key.length();
		if (nodePos <= 0)
			nodePos = 0;

		List<Integer> result = new ArrayList<Integer>();

		char[] keyChars = key.toCharArray();

		int b = base.findValue(nodePos);
		int n;
		int p;

		for (int i = pos; i < len; i++) {
			p = b;
			n = base.findValue(p);

			if(b == check.findValue(p) && n < 0){
				result.add(-n - 1);
			}

			p = b + (int) (keyChars[i]) + 1;
			if(b == check.findValue(p)){
				b = base.findValue(p);
			}else{
				return result;
			}
		}

		p = b;
		n = base.findValue(p);

		if(b == check.findValue(p) && n < 0){
			result.add(-n - 1);
		}

		return result;
	}

	public List<String> commonPrefixSearchList(String content){
		List<String> matchList = new ArrayList<String>();
		
		int len = content.length();
		int nodePos = 0;

		char[] keyChars = content.toCharArray();

		String ansStr = "";
		String tmpStr = "";
		int n;
		int p;
		int b;
		int kk = -1;	//保存中间匹配索引
		for(int ii = 0; ii < len; ii++){
			b = base.findValue(nodePos);

			int i = ii;
			for (; i < len; i++) {
				p = b;
				n = base.findValue(p);

				if (b == check.findValue(p) && n < 0) {
					kk = i;
					tmpStr = ansStr;
				}

				p = b + (int) (keyChars[i]) + 1;
				if (b == check.findValue(p)) {
					b = base.findValue(p);
				} else {
					break;
				}
				
				ansStr += keyChars[i];
			}

			p = b;
			n = base.findValue(p);

			if (n < 0 && b == check.findValue(p)) {
				ii = i - 1;		//最后匹配到了，就从最后索引开始进行下一轮匹配
				matchList.add(ansStr);
			}else if(kk > 0){	//说明中间有部分匹配成功
				ii = kk - 1;	//最后没有匹配成功，但是中间有部分匹配成功了，就从最大匹配成功索引开始进行下一轮匹配
				matchList.add(tmpStr);
			}
			kk = -1;	//还原
			tmpStr = "";
			ansStr = "";
		}
		
		return matchList;
	}
	
	
	// debug
	public void dump() {
		for (int i = 0; i < size; i++) {
			System.err.println("i: " + i + " [" + base.findValue(i) + ", " + check.findValue(i)
					+ "]");
		}
	}
}