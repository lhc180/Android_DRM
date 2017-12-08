package cn.com.pyc.szpbb.coreui;

import com.artifex.mupdfdemo.OutlineItem;
import com.artifex.mupdfdemo.TextWord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.com.pyc.szpbb.coreui.widget.SZPDFCore;
import cn.com.pyc.szpbb.coreui.widget.SZPDFDocView;
import cn.com.pyc.szpbb.sdk.database.bean.SZBookmark;
import cn.com.pyc.szpbb.sdk.database.practice.BookmarkDAOImpl;

/**
 * Created by hudq on 2016/12/8.
 */

public class SZPDFHelpInterface {

    /**
     * 获取pdf的总页码
     *
     * @param core pdf核心core类
     * @return int, 总页码
     */
    public static final int getTotalPages(SZPDFCore core) {
        return core.countPages();
    }

    /**
     * 当前页码
     *
     * @param mDocView pdf视图，{@link SZPDFDocView}
     * @return int, 当前页码
     */
    public static final int getCurrentPage(SZPDFDocView mDocView) {
        return mDocView.getDisplayedViewIndex();
    }

    /**
     * 设置当前页码
     *
     * @param mDocView pdf视图，{@link SZPDFDocView}
     * @param page     当前页码
     */
    public static final void setCurrentPage(SZPDFDocView mDocView, int page) {
        mDocView.setDisplayedViewIndex(page);
    }

    /**
     * 添加书签,如果已经存在就更新书签
     *
     * @param core   Pdf核心core类
     * @param fileId 文件id
     * @param page   页码
     * @return boolean
     */
    public static final boolean saveBookmark(SZPDFCore core, String fileId, int page) {
        return addBookmark(core, fileId, page);
    }

    private static boolean addBookmark(SZPDFCore core, String fileId, int page) {
        boolean result;
        SZBookmark bookmark = SZPDFHelpInterface.getBookmark(fileId, page);
        if (bookmark == null) {
            String content = getPageContent(core, page);
            bookmark = new SZBookmark();
            bookmark.setId(System.currentTimeMillis() + "");
            bookmark.setContent_id(fileId);
            bookmark.setContent(content);
            bookmark.setTime(System.currentTimeMillis());
            bookmark.setPagefew(page);
            result = BookmarkDAOImpl.getInstance().save(bookmark);
        } else {
            bookmark.setTime(System.currentTimeMillis());
            bookmark.setPagefew(page);
            int c = BookmarkDAOImpl.getInstance().update(bookmark);
            result = (c > 0);
        }
        return result;
    }

    private static String getPageContent(SZPDFCore core, int page) {
        String content = "";
        TextWord[][] textWords = core.textLines(page);
        for (TextWord[] textWord : textWords) {
            for (TextWord word : textWord) {
                String w = word.getW();
                content += w;
            }
        }
        content = content.replace(" ", "").trim();
        if ("".equals(content)) {
            content = "[图片]";
        } else {
            final int maxLetters = 61;
            int originalLength = content.length();
            content = content.substring(0, originalLength < maxLetters ? originalLength
                    : maxLetters);
            content = originalLength < maxLetters ? content : content + "...";
        }
        return content;
    }


    /**
     * 根据文件的fileId和页码获取书签
     *
     * @param fileId 对应AlbumContent中的contentId
     * @param page   页码
     * @return {@link SZBookmark}
     */
    public static final SZBookmark getBookmark(String fileId, int page) {
        return BookmarkDAOImpl.getInstance().findBookmarkById(fileId, page);
    }

    /**
     * 根据文件fileId获取所有的书签
     *
     * @param fileId 对应AlbumContent中的contentId
     * @return List
     */
    public static final List<SZBookmark> getBookmark(String fileId) {
        return BookmarkDAOImpl.getInstance().findAllBookmarkById(fileId);
    }

    /**
     * 删除该文件下所有书签
     *
     * @param fileId 文件id
     */
    public static final void removeBookmark(String fileId) {
        removeBookmark(fileId, -1);
    }

    /**
     * 删除指定页码书签
     *
     * @param fileId 文件id
     * @param page   页码，若page < 0，则清除该文件下全部书签
     */
    public static final void removeBookmark(String fileId, int page) {
        if (page < 0) {
            BookmarkDAOImpl.getInstance().deleteBookMark(fileId);
        } else {
            BookmarkDAOImpl.getInstance().deleteBookMarkByPage(fileId, page);
        }
    }

    /**
     * 获取pdf的目录
     *
     * @param core Pdf核心core类
     * @return List
     */
    public static final List<OutlineItem> getCatalogs(SZPDFCore core) {
        List<OutlineItem> items = new ArrayList<>();
        if (SZPDFInterface.isValid(core)) {
            OutlineItem[] outlines = core.getOutline();
            if (outlines != null) {
                items = Arrays.asList(outlines);
            }
        }
        return items;
    }
}
