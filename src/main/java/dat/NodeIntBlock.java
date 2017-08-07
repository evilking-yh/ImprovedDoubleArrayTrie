package dat;

public class NodeIntBlock {
	private static final int BLOCK_SIZE = 10000;	//数组块的大小，可根据数据量来调节该参数，数据量大，则参数应设置大一点，构建速度会提高很多
	
	private int[] value;	//保存的数组实际内容		[0,BLOCK_SIZE - 1]
	private int left;		//数组块的左边界		[left,right]
	private int right;		//数组块的右边界
	private NodeIntBlock next;	//数组块的下一个指针
	
	
	public NodeIntBlock(){
		this.value = new int[BLOCK_SIZE];
		this.left = 0;
		this.right = left + BLOCK_SIZE - 1;
	}
	
	public NodeIntBlock(int left){
		this.value = new int[BLOCK_SIZE];
		this.left = left;
		this.right = left + BLOCK_SIZE - 1;
	}
	
	/**
	 * 返回index指向的元素
	 * @param index
	 * @return
	 */
	public int findValue(int index){
		if(index >= left && index <= right){
			return value[index - left];
		}else if(next == null && index > right){
			return 0;	//代表还未被创建的节点，对应的值当然为0了
		}else{
			return next.findValue(index);
		}
	}
	
	/**
	 * 设置index指向的值
	 * @param index
	 * @param val
	 */
	public void setValue(int index, int val){
		if(index >= left && index <= right){
			value[index - left] = val;
		}else if(next == null && index > right) {
			next = new NodeIntBlock(right + 1);	//自动创建下一个数组块
			next.setValue(index, val);
		}else{
			next.setValue(index,val);
		}
	}
	
	public void dump(){
		for(int i = left ; i < right; i++ ){
			System.out.print(value[i] + " ");
		}
		System.out.println();
		next.dump();		
	}
	
}
