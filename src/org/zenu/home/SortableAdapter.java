package org.zenu.home;

public interface SortableAdapter
{
	void switchingPosition(int from, int to);
	void shiftPosition(int from, int to);
	void appendLast(int from);
}
