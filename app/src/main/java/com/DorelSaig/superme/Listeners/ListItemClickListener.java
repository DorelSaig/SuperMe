package com.DorelSaig.superme.Listeners;

import com.DorelSaig.superme.Objects.MyList;

public interface ListItemClickListener {
    void listItemClicked(MyList myList, int position);
    void listItemLongClick(MyList myList, int position);
}
