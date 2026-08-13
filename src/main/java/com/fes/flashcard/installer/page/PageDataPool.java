package com.fes.flashcard.installer.page;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PageDataPool {

    private final List<PageData> pageDataList = new ArrayList<>();

    public PageDataPool() {}

    public void add(PageData pageData) {
        pageDataList.add(pageData);
    }

    public <T extends PageData> Optional<T> getPageData(Class<T> pageDataClass) {
        return pageDataList.stream().filter(pageDataClass::isInstance).map(pageDataClass::cast).findFirst();
    }
}
