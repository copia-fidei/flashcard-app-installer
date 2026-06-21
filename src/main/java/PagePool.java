import java.util.ArrayList;
import java.util.List;

public class PagePool {

	private final List<Page>    pages = new ArrayList<>();
	private final PageTitleList pageTitleList;
	private final PageFrame     pageFrame;
	private final PageDataPool  pageDataPool;

	private       Page          currentPage;


	public PagePool(PageTitleList pageTitleList, PageFrame pageFrame) {
		this.pageTitleList = pageTitleList;
		this.pageFrame = pageFrame;
		this.pageDataPool = new PageDataPool();
	}

	public void init() {
		var karafPageData = new KarafPageData(pageDataPool);
		pageDataPool.add(karafPageData);
		var karafPage = new KarafPage(karafPageData);
		pages.add(karafPage);

		var databasePageData = new DatabasePageData(pageDataPool);
		pageDataPool.add(databasePageData);
		var databasePage = new DatabasePage(databasePageData);
		pages.add(databasePage);


		for (Page page : pages) {
			page.build();
		}

		pageTitleList.setListData(pages.stream().map(Page::getTitle).toArray(String[]::new));

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

		pageTitleList.setSelectedValue(newPage.getTitle(), true);
		pageFrame.showPage(newPage);
	}

	public void showFirstPage() {
		Page firstPage = pages.getFirst();
		switchPage(firstPage);
	}
}
