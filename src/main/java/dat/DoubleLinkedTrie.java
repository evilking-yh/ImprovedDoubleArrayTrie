package dat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import trie.Branch;
import trie.Forest;
import trie.WoodInterface;

public class DoubleLinkedTrie {
	private NodeIntBlock check;
	private NodeIntBlock base;
	private NodeBoolBlock used;
	
	private int size;
	private int nextCheckPos;
	int error_;
	
	private Random random = new Random();
	
	public DoubleLinkedTrie() {
		check = new NodeIntBlock();
		base = new NodeIntBlock();
		used = new NodeBoolBlock();
		size = 0;
		error_ = 0;
	}
	
	public int build(List<String> key){
		//先构建一颗字典树
		Forest forest = new Forest();
		char[] cs = null;
		for(String line: key){
			cs = line.toCharArray();
			
			WoodInterface tmp = forest;
			for(char c: cs){
				tmp = tmp.add(new Branch(c,false,null));
			}
			tmp.add(new Branch('\000',true,null));    //结尾的空子节点
		}
		key = null;		//释放该列表对象，已将数据转移到了forest对象中
		
		//通过trie树构建double array trie
		base.setValue(0, 1);
		nextCheckPos = 0;
		
		buildDoubleArray(forest);
		
		used = null;
		forest = null;	//不需要该trie树，已将数据转移到双数组中
		
		return error_;
	}
	
	private int buildDoubleArray(WoodInterface node){
		if(error_ < 0){
			return 0;
		}
		
		WoodInterface[] childs = node.getBranchs();	//得到子节点
		
		int begin = 0;
		int pos = ((childs[0].getC() + 1 > nextCheckPos) ? childs[0].getC() + 1 : nextCheckPos) - 1;
		
		int nonzero_num = 0;
		int first = 0;
		
		outer: while(true){
			pos++;
			
			if(check.findValue(pos) != 0){
				nonzero_num++;
				
				pos = (random.nextFloat() < 0.6) ? pos + 1 : pos + 2;  //第一次就冲突，说明数组比较密集了
				
				continue;
			}else if(first == 0){
				nextCheckPos = pos;
				first = 1;
			}
			
			begin = pos - childs[0].getC();
			if(used.findValue(begin)){
				pos = (random.nextFloat() < 0.6) ? pos + 1 : pos + 2;
				
				continue;
			}
			
			for(int i = 1;i < childs.length; i++){
				if(check.findValue(begin + childs[i].getC()) != 0){
					pos = (random.nextFloat() < 0.6) ? pos : pos + 1;  //其他子兄弟节点冲突，说明数组有点密集
					
					continue outer;
				}
			}
			
			break;
		}
		
		//此为冲突率，冲突率调大一点，则数组密集一点，占用内存就少一些；冲突率调小一点，则数组稀疏一些，构建快一点，但是占用内存多一些
		if(1.0*nonzero_num / (pos - nextCheckPos + 1) >= 0.95)
			nextCheckPos = pos;    //更新下一个开始检查点，可以跳过前面一段密集区域
		
		used.setValue(begin, true);
		size = (size > begin + childs[childs.length - 1].getC() + 1) ? size : begin + childs[childs.length - 1].getC() + 1;
		
		for(int i = 0; i < childs.length; i++){
			check.setValue(begin + childs[i].getC(), begin);
		}
		
		for(int i = 0;i < childs.length; i++){
			if(childs[i].getChildCount() == 0){		//到达叶子节点
				base.setValue(begin + childs[i].getC(), -1);	//将模式结尾标记为负
			}else{
				int h = buildDoubleArray(childs[i]);
				base.setValue(begin + childs[i].getC(), h);
			}
		}
		
		return begin;
	}
	
	//模式匹配单个字符串
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
//			p = b + (int) (keyChars[i]) + 1;
			p = b + (int) (keyChars[i]);
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

//			p = b + (int) (keyChars[i]) + 1;
			p = b + (int) (keyChars[i]);
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

//				p = b + (int) (keyChars[i]) + 1;
				p = b + (int) (keyChars[i]);
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
	
	void clear() {
		check = new NodeIntBlock();
		base = new NodeIntBlock();
		used = new NodeBoolBlock();
		size = 0;
	}

	public int getSize() {
		return size;
	}

	public int getNonzeroSize() {
		int result = 0;
		for (int i = 0; i < size; i++)
			if(check.findValue(i) != 0)
				result++;
		return result;
	}
	
}
