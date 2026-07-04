package com.fes.flashcard.installer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PagePool {

	private final List<Page>   pages = new ArrayList<>();
	private final PageFrame    pageFrame;
	private final PageDataPool pageDataPool;

	private Page currentPage;


	public PagePool(PageFrame pageFrame) {
		this.pageFrame = pageFrame;
		this.pageDataPool = new PageDataPool();
	}

	public void init() {
		var karafPageData = new KarafPageData(pageDataPool);
		pageDataPool.add(karafPageData);
		var karafPage = new KarafPage(karafPageData, pageFrame.getButtonsBar());
		pages.add(karafPage);

		var databasePageData = new DatabasePageData(pageDataPool);
		pageDataPool.add(databasePageData);
		var databasePage = new DatabasePage(databasePageData, pageFrame.getButtonsBar());
		pages.add(databasePage);


		for (Page page : pages) {
			page.build();
		}

		pageFrame.getPageTitleList().setListData(pages.stream().map(Page::getTitle).toArray(String[]::new));

		var buttonsBar = pageFrame.getButtonsBar();
		buttonsBar.getNextButton().addActionListener(_ -> forward());
		buttonsBar.getBackButton().addActionListener(_ -> back());
	}

	public void forward() {
		int currentIndex = pages.indexOf(currentPage);
		if (currentIndex < pages.size() - 1) {
			Page nextPage = pages.get(currentIndex + 1);
			switchPage(nextPage);
		}
	}

	public void back() {
		int currentIndex = pages.indexOf(currentPage);
		if (currentIndex > 0) {
			Page previousPage = pages.get(currentIndex - 1);
			switchPage(previousPage);
		}
	}

	private void switchPage(Page newPage) {
		if (currentPage != null) {
			currentPage.willBecomeInvisible();
		}
		currentPage = newPage;
		currentPage.willBecomeVisible();


//		JLayer<Page> layer = new JLayer<>(newPage, new ToastLayerUI());

		pageFrame.getPageTitleList().setSelectedValue(newPage.getTitle(), true);
		pageFrame.showPage(newPage);
	}



	public void showFirstPage() {
		if (pages.isEmpty()) return;

		Dimension biggest = null;
		for (Page page : pages) {
			var preferredSize = page.content.getPreferredSize();
			if (biggest == null) {
				biggest = preferredSize;
			}
			if (preferredSize.height > biggest.height && preferredSize.width > biggest.width) {
				biggest = preferredSize;
			}
		}
		var firstPage = pages.getFirst();
		firstPage.content.setPreferredSize(new Dimension(biggest.width + 50, biggest.height + 50));
		switchPage(firstPage);
	}
}
