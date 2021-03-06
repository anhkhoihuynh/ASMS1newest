package com.project.asms1.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.project.asms1.R;
import com.project.asms1.Utils.SecurityLogic;
import com.project.asms1.config.MyConfig;
import com.project.asms1.daos.OrderDAO;
import com.project.asms1.daos.PostDAO;
import com.project.asms1.daos.ProductDAO;
import com.project.asms1.daos.UserDAO;
import com.project.asms1.model.Order;
import com.project.asms1.model.Post;
import com.project.asms1.model.Product;
import com.project.asms1.model.Store;
import com.project.asms1.model.Token;
import com.project.asms1.model.User;
import com.project.asms1.network.service.APIService;
import com.project.asms1.presentation.AccountDetailActivity;
import com.project.asms1.presentation.AdminHomeActivity;
import com.project.asms1.presentation.CreateAccountActivity;
import com.project.asms1.presentation.LoginActivity;
import com.project.asms1.presentation.ManageAccountActivity;
import com.project.asms1.presentation.OrderListActivity;
import com.project.asms1.presentation.PostListActivity;
import com.project.asms1.presentation.SellerHomeActivity;
import com.project.asms1.presentation.ui.store.StoreFragment;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserUIService {
    private static final String TAG = UserUIService.class.getSimpleName();

    public static void deleteToken(Context context) {
        try {
            SecurityLogic.getPreferenceInstance(context);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String tokens = SecurityLogic.getTokens();

        if (tokens != null) {
            NetworkProvider nw = NetworkProvider.self();
            nw.getService(APIService.class).DeleteToken(tokens).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.isSuccessful()) {
                        String result = response.body();
                        if (result.equals(MyConfig.SUCCESS)) {
                            SecurityLogic.deleteTokens();
                        }else {
                            System.out.println("fail1");
                        }
                    }else {
                        System.out.println("Fail2");
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e(TAG, t.getMessage());
                }
            });
        }else {
            System.out.println("Here2");
        }
    }

    public static void checkTokens(Context context, CircularProgressButton btn) {
        try {
            SecurityLogic.getPreferenceInstance(context);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String tokens = SecurityLogic.getTokens();
        System.out.println(tokens);

        if (tokens != null) {
            NetworkProvider nw = NetworkProvider.self();
            nw.getService(APIService.class).Loading(new Token(tokens,MyConfig.productperpage)).enqueue(new Callback<Token>() {
                @Override
                public void onResponse(Call<Token> call, Response<Token> response) {
                    if (response.isSuccessful()) {
                        Token result = response.body();
                        System.out.println(tokens);
                        if (result.getResult().equals(MyConfig.SUCCESS)) {
                            btn.doneLoadingAnimation(ContextCompat.getColor(context,R.color.purple),
                                    BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_done_white_48dp));
                            UserDAO.currentUser = result.getUser();
                            if (UserDAO.currentUser.getRole() == 1) {
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(context, AdminHomeActivity.class);
                                        context.startActivity(intent);
                                    }
                                }, 1000);
                            }else {
                                ProductDAO.listOfProduct = result.getListProducts();
                                ProductDAO.numberOfPage = result.getNumberOfPage();
                                ProductDAO.listOfCategory = result.getCategoryslist();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(context, SellerHomeActivity.class);
                                        context.startActivity(intent);
                                    }
                                }, 1000);
                            }
                        }
                        else if(result.getResult().equals(MyConfig.deactive)) {
                            btn.doneLoadingAnimation(ContextCompat.getColor(context,R.color.black),
                                    BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_pregnant_woman_white_48dp));
                            Intent intent = new Intent(context,LoginActivity.class);
                            intent.putExtra("message",MyConfig.deactivemessage);
                            context.startActivity(intent);
                        }else {
                            btn.doneLoadingAnimation(ContextCompat.getColor(context,R.color.black),
                                    BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_pregnant_woman_white_48dp));
                            Intent intent = new Intent(context,LoginActivity.class);
                            context.startActivity(intent);
                        }
                    }else {
                        btn.doneLoadingAnimation(ContextCompat.getColor(context,R.color.black),
                                BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_pregnant_woman_white_48dp));
                        Intent intent = new Intent(context,LoginActivity.class);
                        context.startActivity(intent);
                    }
                }
                @Override
                public void onFailure(Call<Token> call, Throwable t) {
                    Log.e(TAG, t.getMessage());
                }
            });
        }else {
            System.out.println("Here4");
            btn.doneLoadingAnimation(ContextCompat.getColor(context,R.color.black),
                    BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_pregnant_woman_white_48dp));
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(context,LoginActivity.class);
                    context.startActivity(intent);
                }
            }, 1000);
        }
    }

    public static void updateUser(Activity context,User user) {
        NetworkProvider nw = NetworkProvider.self();
        if (UserDAO.currentUser == null) {
            System.out.println("vc current null r");
        }
        System.out.println(UserDAO.currentUser);
        nw.getService(APIService.class).Update(user).enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful()) {
                    Token result = response.body();
                    if(context instanceof AccountDetailActivity) {
                        UserDAO.flag = true;
                        ((AccountDetailActivity) context).onBackPressed();
                    }
                }else {
                    System.out.println("Fail9");
                }
            }
            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public static void changePage(int position, String currentCategory, StoreFragment storeFragment) {
        NetworkProvider nw = NetworkProvider.self();
        System.out.println(ProductDAO.currentCategory + "-" + position);
        nw.getService(APIService.class).changePage(position,MyConfig.productperpage,currentCategory).enqueue(new Callback<Store>() {
            @Override
            public void onResponse(Call<Store> call, Response<Store> response) {
                if (response.isSuccessful()) {
                    Store result = response.body();
                    ProductDAO.listOfProduct = result.getListProducts();
                    storeFragment.changePage();
                }else {
                    System.out.println("Fail change Page");
                }
            }
            @Override
            public void onFailure(Call<Store> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });


    }

    public static void changeCategory(int position, String currentCategory, StoreFragment storeFragment) {
        NetworkProvider nw = NetworkProvider.self();
        System.out.println(ProductDAO.currentCategory + "-" + position);
        nw.getService(APIService.class).changePage(position,MyConfig.productperpage,currentCategory).enqueue(new Callback<Store>() {
            @Override
            public void onResponse(Call<Store> call, Response<Store> response) {
                if (response.isSuccessful()) {
                    Store result = response.body();
                    ProductDAO.listOfProduct = result.getListProducts();
                    ProductDAO.numberOfPage = result.getSumofpages();
                    storeFragment.changListOfPage();
                }else {
                    System.out.println("Fail change Page");
                }
            }
            @Override
            public void onFailure(Call<Store> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });


    }

    public static void searchProduct(String searchString, StoreFragment storeFragment) {
        NetworkProvider nw = NetworkProvider.self();
        System.out.println(ProductDAO.currentCategory);
        nw.getService(APIService.class).searchProduct(MyConfig.productperpage,searchString).enqueue(new Callback<Store>() {
            @Override
            public void onResponse(Call<Store> call, Response<Store> response) {
                if (response.isSuccessful()) {
                    Store result = response.body();
                    if (result.getResult().equals(MyConfig.Fail)) {
                        Toast.makeText(storeFragment.getContext(),"NO ITEMS IS MATCH WITH YOUR SEARCH!",Toast.LENGTH_SHORT).show();
                    }else {
                        ProductDAO.listOfProduct = result.getListProducts();
                        ProductDAO.numberOfPage = result.getSumofpages();
                        storeFragment.changListOfPage();
                    }
                }else {
                    System.out.println("Fail search product");
                }
            }
            @Override
            public void onFailure(Call<Store> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });


    }

    public static void getOrder(int currentPage,int orderperpage,String searchString, OrderListActivity current) {
        NetworkProvider nw = NetworkProvider.self();
        nw.getService(APIService.class).getOrder(currentPage,orderperpage,searchString).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful()) {
                    List<Order> result = response.body();
//                    if (result.getResult().equals(MyConfig.Fail)) {
//                        Toast.makeText(storeFragment.getContext(),"NO ITEMS IS MATCH WITH YOUR SEARCH!",Toast.LENGTH_SHORT).show();
//                    }else {
                        OrderDAO.listOfOrder = result;
                        if(currentPage == 1) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    current.loadFirstPage();
                                }
                            }, 1000);
                        }else {
                            current.loadNextPage();
                        }
//                    }
                }else {
                    System.out.println("Fail get order");
                }
            }
            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });


    }

    public static void getAccount(int currentPage,int accountperpage,String searchString, ManageAccountActivity current) {
        NetworkProvider nw = NetworkProvider.self();
        nw.getService(APIService.class).getAccount(currentPage,accountperpage,searchString).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful()) {
                    List<User> result = response.body();
                    UserDAO.listofuser = result;
                    if(currentPage == 1) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                current.loadFirstPage();
                            }
                        }, 1000);
                    }else {
                        current.loadNextPage();
                    }
                }else {
                    System.out.println("Fail get account");
                }
            }
            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

    public static void getPost(int currentPage,int orderperpage,String searchString, PostListActivity current) {
        NetworkProvider nw = NetworkProvider.self();
        nw.getService(APIService.class).getPost(currentPage, orderperpage, searchString).enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                if (response.isSuccessful()) {
                    List<Post> result = response.body();
                    PostDAO.listofPost = result;
                    if (currentPage == 1) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                current.loadFirstPage();
                            }
                        }, 1000);
                    } else {
                        current.loadNextPage();
                    }
                } else {
                    System.out.println("Fail get order");
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }

        public static void addUser (User user, CreateAccountActivity current){
            NetworkProvider nw = NetworkProvider.self();
            nw.getService(APIService.class).addUser(user).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful()) {
                        User result = response.body();
                        if (result.getMessage().equals(MyConfig.SUCCESS)) {
                            current.showCustomDialog();
                        }else {
                            Toast.makeText(current,"Fail to create user",Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        System.out.println("Fail add user");
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e(TAG, t.getMessage());
                }
            });
        }
}
