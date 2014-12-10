/*
 * Copyright 2014 Mustafa DUMLUPINAR
 * 
 * mdumlupinar@gmail.com
 * 
*/

package meta;

import java.util.List;
import java.util.Map;

import play.i18n.Messages;

import com.avaje.ebean.Page;

/**
 * @author mdpinar
*/
public class PageExtend<T> {

	public List<GridHeader> headers;
	public List<Map<Integer, String>> data;

	public int totalRowCount;
	public int totalPageCount;
	public int pageIndex;
	public String indexOf;
	public boolean next;
	public boolean prev;
	public boolean isMultiPage;

	public PageExtend(List<GridHeader> headers, List<Map<Integer, String>> data, Page<T> page) {
		this.headers = headers;
		this.data = data;

		this.totalRowCount = data.size();
		this.totalPageCount = 1;
		this.pageIndex = 1;

		if (page != null) {
			this.totalRowCount = page.getTotalRowCount();
			this.totalPageCount = page.getTotalPageCount();
			this.pageIndex = page.getPageIndex();
			this.next = page.hasNext();
			this.prev = page.hasPrev();

			if (totalPageCount > 1) {
				this.isMultiPage = true;
				this.indexOf = String.format("%s : %s / %s", Messages.get("reporter.page"), (page.getPageIndex() + 1), page.getTotalPageCount());
			}
		}
	}

	public List<GridHeader> getHeaders() {
		return headers;
	}

	public List<Map<Integer, String>> getData() {
		return data;
	}

	public boolean isMultiPage() {
		return isMultiPage;
	}

	public int getTotalRowCount() {
		return totalRowCount;
	}

	public int getTotalPageCount() {
		return totalPageCount;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public String getIndexOf() {
		return indexOf;
	}

	public boolean hasNext() {
		return next;
	}

	public boolean hasPrev() {
		return prev;
	}

}
