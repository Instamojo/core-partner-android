package com.getmeashop.partner.util;

import android.location.Location;

import com.getmeashop.partner.database.User;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;

/**
 * Created by naveen on 6/29/2015.
 */
public class Interfaces {


    public interface removeImg {
        void clickedRemoveImg();
    }


    public interface show_sync_icon {
        void CheckSync();
    }

    public interface UpdateCounter {
        void UpdateProdCounter();

        void UpdateCatCounter();

        void UpdateOrderCounter();
    }

    public interface ItemClick {
        void ItemClicked(String id, boolean to_add_prod);
    }


    public interface SearchResultCount {
        void SearchItemsCount(int num);
    }

    public interface LoadStatistics {
        void LoadStatstics();
    }


    /**
     * interface for callbacks when Get or Post request are created
     */
    public interface PutCallbacks {

        public void postexecute(String url, int status);

        public void preexecute(String url);

        public void processResponse(HttpResponse response, String url);

        public HttpPut preparePutData(String url, HttpPut httpPost);

    }

    public interface shouldNotify {
        void shouldNotify();
    }

    public interface updateView {
        void updateUserList();
    }

    public interface goToFrag {
        void gotoFragment(int pos);
    }

    public interface archive {
        void archive(User user);

        void delete(User user, int position);
    }

    public interface enterLocation{
        void enterLocation(Location loc);
    }

}
