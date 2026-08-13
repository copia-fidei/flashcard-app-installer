package com.fes.flashcard.installer.page;

import com.fes.flashcard.installer.apply.ApplyDialog;
import com.fes.flashcard.installer.operation.Operation;
import org.jetbrains.annotations.NonNls;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

@NonNls
public abstract class PagePool {

	protected final Logger log = getLogger(getClass().getName());

	protected final List<Page>                pages      = new ArrayList<>();
	protected final List<Supplier<Operation>> operations = new ArrayList<>();
	protected final PageFrame    pageFrame;
	protected final PageDataPool pageDataPool;
	protected final ButtonBar    buttonBar;

	private Page currentPage;

	public PagePool(PageFrame pageFrame) {
		this.pageFrame = pageFrame;
		this.buttonBar = pageFrame.getButtonBar();
		this.pageDataPool = new PageDataPool();
	}

	public void init() {
		initOverride();
		for (Page page : pages) {
			page.build();
		}
		pageFrame.getPageTitleList().setListData(getPageTitles());
		buttonBar.getCancelButton().addActionListener(_ -> pageFrame.dispose());
		buttonBar.getNextButton().addActionListener(_ -> forward());
		buttonBar.getBackButton().addActionListener(_ -> back());
		buttonBar.getDefaultsButton().addActionListener(_ -> currentPage.restoreDefaults());
		buttonBar.getApplyButton().addActionListener(_ -> new ApplyDialog(pageFrame, getOperations()).setVisible(true));
	}

	private List<Operation> getOperations() {
		return operations.stream().map(Supplier::get).toList();
	}

	private String[] getPageTitles() {
		return pages.stream().map(Page::getTitle).toArray(String[]::new);
	}

	/**
	 * Create and add the pages and operations.
	 */
	protected abstract void initOverride();

	public void forward() {
		int currentIndex = pages.indexOf(currentPage);
		if (!(currentIndex < pages.size() - 1)) throw new IndexOutOfBoundsException("""
				Cannot go forward.
				Current page index: %d
				Total pages: %d
				""".formatted(currentIndex, pages.size()));
		Page nextPage = pages.get(currentIndex + 1);
		switchPage(nextPage);
		updateButtons();
	}

	protected void updateButtons() {
		int     index         = pages.indexOf(currentPage);
		boolean isTheLastPage = (index >= pages.size() - 1);
		if (isTheLastPage) {
			buttonBar.getNextButton().setEnabled(false);
			buttonBar.getApplyButton().setEnabled(currentPage.isValid());
		} else {
			buttonBar.getNextButton().setEnabled(currentPage.isValid());
			buttonBar.getApplyButton().setEnabled(false);
		}
		buttonBar.getBackButton().setEnabled(!(index <= 0));
	}

	public void back() {
		int currentIndex  = pages.indexOf(currentPage);
		int previousIndex = currentIndex - 1;
		if (currentIndex <= 0) {
			throw new IndexOutOfBoundsException("""
					Cannot go back.
					Current page index: %d
					Total pages: %d
					""".formatted(currentIndex, pages.size()));
		}
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
		if (pages.isEmpty()) {
			log.info("pages is empty");
			return;
		}
		var firstPage = pages.getFirst();
		firstPage.getContent().setPreferredSize(getLargest());
		switchPage(firstPage);
		updateButtons();
	}

	private Dimension getLargest() {
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
		return biggest;
	}
}
