package trie;

public abstract interface WoodInterface {
	public abstract WoodInterface add(WoodInterface paramWoodInterface);

	public abstract WoodInterface get(char paramChar);

	public abstract boolean contains(char paramChar);

	public abstract int compareTo(char paramChar);

	public abstract boolean equals(char paramChar);

	public abstract char getC();
	
	public abstract int getChildCount();
	
	public abstract boolean getStatus();
	
	public abstract void setStatus(boolean status);

	public abstract String[] getParams();

	public abstract void setParam(String[] paramArrayOfString);
	
	public abstract WoodInterface[] getBranchs();
	
}