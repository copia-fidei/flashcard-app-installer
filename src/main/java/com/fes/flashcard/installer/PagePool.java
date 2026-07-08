package com.fes.flashcard.installer;

import com.fes.flashcard.installer.app.pages.DatabasePage;
import com.fes.flashcard.installer.app.pages.DatabasePageData;
import com.fes.flashcard.installer.app.pages.JavaPage;
import com.fes.flashcard.installer.app.pages.JavaPageData;
import com.fes.flashcard.installer.app.pages.KarafPage;
import com.fes.flashcard.installer.app.pages.KarafPageData;
import com.fes.flashcard.installer.app.pages.RootPasswordPage;
import com.fes.flashcard.installer.app.pages.RootPasswordPageData;
import com.fes.flashcard.installer.page.Page;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// This is also application specific, maybe make an abstract class for this
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
		// This is application specific
		var karafPageData = new KarafPageData(pageDataPool);
		pageDataPool.add(karafPageData);
		var karafPage = new KarafPage(karafPageData, this::updateButtons);
		pages.add(karafPage);

		var databasePageData = new DatabasePageData(pageDataPool);
		pageDataPool.add(databasePageData);
		var databasePage = new DatabasePage(databasePageData, this::updateButtons);
		pages.add(databasePage);

		var javaPageData = new JavaPageData(pageDataPool);
		pageDataPool.add(javaPageData);
		var javaPage = new JavaPage(javaPageData, this::updateButtons);
		pages.add(javaPage);

		var rootPwPageData = new RootPasswordPageData(pageDataPool);
		pageDataPool.add(rootPwPageData);
		var rootPwPage = new RootPasswordPage(rootPwPageData, this::updateButtons);
		pages.add(rootPwPage);
		// This is application specific // END


		for (Page page : pages) {
			page.build();
		}

		pageFrame.getPageTitleList().setListData(pages.stream().map(Page::getTitle).toArray(String[]::new));

		var buttonsBar = pageFrame.getButtonsBar();
		buttonsBar.getNextButton().addActionListener(_ -> forward());
		buttonsBar.getBackButton().addActionListener(_ -> back());
		buttonsBar.getDefaultsButton().addActionListener(_ -> currentPage.restoreDefaults());


	}


	public void forward() {
		int currentIndex = pages.indexOf(currentPage);
		if (!(currentIndex < pages.size() - 1)) throw new IndexOutOfBoundsException("Cannot go forward");
		Page nextPage = pages.get(currentIndex + 1);
		switchPage(nextPage);
		updateButtons();
	}

	private void updateButtons() {
		int     index         = pages.indexOf(currentPage);
		boolean isTheLastPage = (index >= pages.size() - 1);
		if (isTheLastPage) {
			pageFrame.getButtonsBar().getNextButton().setEnabled(false);
		} else {
			pageFrame.getButtonsBar().getNextButton().setEnabled(currentPage.isValid());
		}
		pageFrame.getButtonsBar().getBackButton().setEnabled(!(index <= 0));
	}

	public void back() {
		int currentIndex  = pages.indexOf(currentPage);
		int previousIndex = currentIndex - 1;
		if (currentIndex <= 0) throw new IndexOutOfBoundsException("Cannot go back");
		Page previousPage = pages.get(previousIndex);
		switchPage(previousPage);
		updateButtons();
	}

	private void switchPage(Page newPage) {
		if (currentPage != null) {
			currentPage.willBecomeInvisible();
		}
		currentPage = newPage;
		currentPage.willBecomeVisible();

		pageFrame.getPageTitleList().setSelectedValue(newPage.getTitle(), true);
		pageFrame.showPage(newPage);
	}



	public void showFirstPage() {
		if (pages.isEmpty()) return;
		// make the biggest x, and y
		Dimension biggest = new Dimension(0, 0);
		for (Page page : pages) {
			var preferredSize = page.getContent().getPreferredSize();
			if (preferredSize.height > biggest.height) {
				biggest.height = preferredSize.height;
			}
			if (preferredSize.width > biggest.width) {
				biggest.width = preferredSize.width;
			}
		}
		var firstPage = pages.getFirst();
		firstPage.getContent().setPreferredSize(new Dimension(biggest.width + 50, biggest.height + 50));
		switchPage(firstPage);
		updateButtons();
	}
}
