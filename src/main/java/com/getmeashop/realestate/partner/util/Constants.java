package com.getmeashop.realestate.partner.util;

/**
 * Created by nikka on 1/8/15.
 */
public class Constants {
    public final static int FragAddUser = 0;
    public final static int FragManageUser = 1;
    public final static int FragArchivedUser = 2;
    public final static String User_sp = "Users";
    public final static String first_page_sp = "first_page";


    public final static String base_uri = "http://api.dev2.gmastesttest.com/";
    public final static String base_uri1 = "http://api.dev2.gmastesttest.com";


    public final static String base_suffix = ".staging.getmeashop.org";
    public final static String GCM_SENDER_ID = "279782731317";

//    public final static String base_uri = "http://www.mbtest.angrynerd.in/";
//    public final static String base_uri1 = "http://www.mbtest.angrynerd.in";


    public final static String hosted_url = "http://updates.getmeashop.org/api/android/apps/?app=com.getmeashop.realestate.partner";
    public final static String update_check_url = "http://updates.getmeashop.org/api/android/apps/?app=com.getmeashop.realestate.partner";
    public final static String uri_get_user = base_uri + "api/partner/users/?format=json&limit=10&order_by=-modified";
    public final static String uri_send_user = base_uri + "api/partner/users/?format=json";
    public final static String uri_store_info = base_uri + "api/partner/storeinfo/";
    public final static String uri_shipping_info = base_uri + "api/partner/shipping/";
    public final static String uri_pay_info = base_uri + "api/partner/userchequepayment/";
    public final static String uri_db_search = base_uri + "api/partner/users/?format=json&username__contains=";
    public final static String uri_lookup_domain = base_uri + "api/partner/domain-suggestion/";
    public final static String uri_domain_register = base_uri + "api/partner/domain-suggestion/";
}
