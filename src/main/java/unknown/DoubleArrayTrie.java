/*
 * Name:   Double Array Trie
 * Author: Yaguang Ding
 * Date:   2012/5/21
 * Note: a word ends may be either of these two case:
 * 1. Base[cur_p] == pos  ( pos<0 and Tail[-pos] == 'END_CHAR' )
 * 2. Check[Base[cur_p] + Code('END_CHAR')] ==  cur_p
 */
package unknown;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;


public class DoubleArrayTrie {
	final char END_CHAR = '\0';
	final int DEFAULT_LEN = 1024;
	int Base[]  = new int [DEFAULT_LEN];
	int Check[] = new int [DEFAULT_LEN];
	char Tail[] = new char [DEFAULT_LEN];
	int Pos = 1;
	Map<Character ,Integer> CharMap = new HashMap<Character,Integer>();
	ArrayList<Character> CharList = new ArrayList<Character>();
	
	public DoubleArrayTrie()
	{
		Base[1] = 1;
		
		CharMap.put(END_CHAR,1);
		CharList.add(END_CHAR);
		CharList.add(END_CHAR);
		for(int i=0;i<26;++i)
		{
			CharMap.put((char)('a'+i),CharMap.size()+1);
			CharList.add((char)('a'+i));
		}
		
	}
	private void Extend_Array()
	{
		Base = Arrays.copyOf(Base, Base.length*2);
		Check = Arrays.copyOf(Check, Check.length*2);
	}
	
	private void Extend_Tail()
	{
		Tail = Arrays.copyOf(Tail, Tail.length*2);
	}
	
	private int GetCharCode(char c)
	{
		if (!CharMap.containsKey(c))
		{
			CharMap.put(c,CharMap.size()+1);
			CharList.add(c);
		}
		return CharMap.get(c);
	}
	private int CopyToTailArray(String s,int p)
	{
		int _Pos = Pos;
		while(s.length()-p+1 > Tail.length-Pos)
		{
			Extend_Tail();
		}
		for(int i=p; i<s.length();++i)
		{
			Tail[_Pos] = s.charAt(i);
			_Pos++;
		}
		return _Pos;
	}
	
	private int x_check(Integer []set)
	{
		for(int i=1; ; ++i)
		{
			boolean flag = true;
			for(int j=0;j<set.length;++j)
			{
				int cur_p = i+set[j];
				if(cur_p>= Base.length) Extend_Array();
				if(Base[cur_p]!= 0 || Check[cur_p]!= 0)
				{
					flag = false;
					break;
				}
			}
			if (flag) return i;
		}
	}
	
	private ArrayList<Integer> GetChildList(int p)
	{
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for(int i=1; i<=CharMap.size();++i)
		{
			if(Base[p]+i >= Check.length) break;
			if(Check[Base[p]+i] == p)
			{
				ret.add(i);
			}
		}
		return ret;
	}
	
	private boolean TailContainString(int start,String s2)
	{
		for(int i=0;i<s2.length();++i)
		{
			if(s2.charAt(i) != Tail[i+start]) return false;
		}
		
		return true;
	}
	private boolean TailMatchString(int start,String s2)
	{
		s2 += END_CHAR;
		for(int i=0;i<s2.length();++i)
		{
			if(s2.charAt(i) != Tail[i+start]) return false;
		}
		return true;
	}
	
	
	public void Insert(String s) throws Exception
	{
		s += END_CHAR;
		
		int pre_p = 1;
		int cur_p;
		for(int i=0; i<s.length(); ++i)
		{
			//��ȡ״̬λ��
			cur_p = Base[pre_p]+GetCharCode(s.charAt(i));
			//������ȳ������У���չ����
			if (cur_p >= Base.length) Extend_Array();
			
			//����״̬
			if(Base[cur_p] == 0 && Check[cur_p] == 0)
			{
				Base[cur_p] = -Pos;
				Check[cur_p] = pre_p;
				Pos = CopyToTailArray(s,i+1);
				break;
			}else
			//�Ѵ���״̬
			if(Base[cur_p] > 0 && Check[cur_p] == pre_p)
			{
				pre_p = cur_p;
				continue;
			}else
			//��ͻ 1������ Base[cur_p]С��0�ģ�������һ����ѹ���浽Tail�е��ַ���
			if(Base[cur_p] < 0 && Check[cur_p] == pre_p)
			{
				int head = -Base[cur_p];
				
				if(s.charAt(i+1)== END_CHAR && Tail[head]==END_CHAR)	//�����ظ��ַ���
				{
					break;
				}
				
				//������ĸ���������Ϊ��һ���ж��Ѿ��ų��˽�����������һ����2�������ǽ�����
				if (Tail[head] == s.charAt(i+1))
				{
					int avail_base = x_check(new Integer[]{GetCharCode(s.charAt(i+1))});
					Base[cur_p] = avail_base;
					
					Check[avail_base+GetCharCode(s.charAt(i+1))] = cur_p;
					Base[avail_base+GetCharCode(s.charAt(i+1))] = -(head+1);
					pre_p = cur_p;
					continue;
				}
				else
				{
					//2����ĸ����ͬ�������������һ��Ϊ������
					int avail_base ;
					avail_base = x_check(new Integer[]{GetCharCode(s.charAt(i+1)),GetCharCode(Tail[head])});
					
					Base[cur_p] = avail_base;
					
					Check[avail_base+GetCharCode(Tail[head])] = cur_p;
					Check[avail_base+GetCharCode(s.charAt(i+1))] = cur_p;
					
					//Tail ΪEND_FLAG �����
					if(Tail[head] == END_CHAR)
						Base[avail_base+GetCharCode(Tail[head])] = 0;
					else
						Base[avail_base+GetCharCode(Tail[head])] = -(head+1);
					if(s.charAt(i+1) == END_CHAR) 
						Base[avail_base+GetCharCode(s.charAt(i+1))] = 0;
					else
						Base[avail_base+GetCharCode(s.charAt(i+1))] = -Pos;
					
					Pos = CopyToTailArray(s,i+2);
					break;
				}
			}else
			//��ͻ2����ǰ����Ѿ���ռ�ã���Ҫ����pre��base
			if(Check[cur_p] != pre_p)
			{
				ArrayList<Integer> list1 = GetChildList(pre_p);
				int toBeAdjust;
				ArrayList<Integer> list = null;
				if(true)
				{
					toBeAdjust = pre_p;
					list = list1;
				}
				
				int origin_base = Base[toBeAdjust];
				list.add(GetCharCode(s.charAt(i)));
				int avail_base = x_check((Integer[])list.toArray(new Integer[list.size()]));
				list.remove(list.size()-1);
				
				Base[toBeAdjust] = avail_base;
				for(int j=0; j<list.size(); ++j)
				{
					//BUG 
					int tmp1 = origin_base + list.get(j);
					int tmp2 = avail_base + list.get(j);
					
					Base[tmp2] = Base[tmp1];
					Check[tmp2] = Check[tmp1];
					
					//�к���
					if(Base[tmp1] > 0)
					{
						ArrayList<Integer> subsequence = GetChildList(tmp1);
						for(int k=0; k<subsequence.size(); ++k)
						{
							Check[Base[tmp1]+subsequence.get(k)] = tmp2;
						}
					}
					
					Base[tmp1] = 0;
					Check[tmp1] = 0;
				}
				
				//�����µ�cur_p
				cur_p = Base[pre_p]+GetCharCode(s.charAt(i));
				
				if(s.charAt(i) == END_CHAR)
					Base[cur_p] = 0;
				else
					Base[cur_p] = -Pos;
				Check[cur_p] = pre_p;
				Pos = CopyToTailArray(s,i+1);
				break;
			}
		}
	}
	
	public boolean Exists(String word)
	{
		int pre_p = 1;
		int cur_p = 0;
		
		for(int i=0;i<word.length();++i)
		{
			cur_p = Base[pre_p]+GetCharCode(word.charAt(i));
			if(Check[cur_p] != pre_p) return false;
			if(Base[cur_p] < 0)
			{
				if(TailMatchString(-Base[cur_p],word.substring(i+1)))
					return true;
				return false;
			}
			pre_p = cur_p;
		}
		if(Check[Base[cur_p]+GetCharCode(END_CHAR)] == cur_p)
			return true;
		return false;
	}
	
	//�ڲ�����������ƥ�䵥�ʵ�����Base index��
	class FindStruct
	{
		int p;
		String prefix="";
	}
	private FindStruct Find(String word)
	{
		int pre_p = 1;
		int cur_p = 0;
		FindStruct fs = new FindStruct();
		for(int i=0;i<word.length();++i)
		{
			// BUG
			fs.prefix += word.charAt(i);
			cur_p = Base[pre_p]+GetCharCode(word.charAt(i));
			if(Check[cur_p] != pre_p)
			{
				fs.p = -1;
				return fs;
			}
			if(Base[cur_p] < 0)
			{
				if(TailContainString(-Base[cur_p],word.substring(i+1)))
				{
					fs.p = cur_p;
					return fs;
				}
				fs.p = -1;
				return fs;
			}
			pre_p = cur_p;
		}
		fs.p =  cur_p;
		return fs;
	}
	
	public ArrayList<String> GetAllChildWord(int index)
	{
		ArrayList<String> result = new ArrayList<String>();
		if(Base[index] == 0)
		{
			result.add("");
			return result;
		}
		if(Base[index] < 0)
		{
			String r="";
			for(int i=-Base[index];Tail[i]!=END_CHAR;++i)
			{
				r+= Tail[i];
			}
			result.add(r);
			return result;
		}
		for(int i=1;i<=CharMap.size();++i)
		{
			if(Check[Base[index]+i] == index)
			{
				for(String s:GetAllChildWord(Base[index]+i))
				{
					result.add(CharList.get(i)+s);
				}
				//result.addAll(GetAllChildWord(Base[index]+i));
			}
		}
		return result;
	}
	
	public ArrayList<String> FindAllWords(String word)
	{
		ArrayList<String> result = new ArrayList<String>();
		String prefix = "";
		FindStruct fs = Find(word);
		int p = fs.p;
		if (p == -1) return result;
		if(Base[p]<0)
		{
			String r="";
			for(int i=-Base[p];Tail[i]!=END_CHAR;++i)
			{
				r+= Tail[i];
			}
			result.add(fs.prefix+r);
			return result;
		}
		
		if(Base[p] > 0)
		{
			ArrayList<String> r =  GetAllChildWord(p);
			for(int i=0;i<r.size();++i)
			{
				r.set(i, fs.prefix+r.get(i));
			}
			return r;
		}
		
		return result;
	}
	
}
