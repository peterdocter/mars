package com.leap.mars.cmp;

import com.leap.mars.model.User;
import com.leap.mars.network.user.usecase.UserGetCase;
import com.leap.mini.mgr.StorageMgr;
import com.leap.mini.mgr.TokenMgr;
import com.leap.mini.net.HttpSubscriber;
import com.leap.mini.model.network.Response;
import com.leap.mini.util.IsEmpty;

import java.util.Timer;
import java.util.TimerTask;

import static com.leap.mini.mgr.StorageMgr.LEVEL_GLOBAL;

/**
 * Created by fhx on 16/11/10. 封装Session
 */
public class SessionMgr {
  // key
  private static String KEY_SESSION = "session";
  private static User user;
  private static Timer timer;

  public static void init() {
    timer = new Timer(true);
    timer.schedule(new Task(), 1000, 1000 * 60 * 60);
    user = StorageMgr.get(KEY_SESSION, User.class, LEVEL_GLOBAL);
    if (IsEmpty.object(user)) {
      user = new User();
    }
  }

  // 更新用户
  public static void updateUser(User user) {
    SessionMgr.user = user;
    StorageMgr.set(KEY_SESSION, user, LEVEL_GLOBAL);
  }

  public static User getUser() {
    return user;
  }

  /**
   * 会话消息任务定时管理器
   */
  private static class Task extends TimerTask {
    @Override
    public void run() {
      if (TokenMgr.hasUser()) {
        new UserGetCase(user.getId()).execute(new HttpSubscriber<User>(null) {
          @Override
          public void onSuccess(Response<User> response) {
            SessionMgr.updateUser(response.getData());
          }

          @Override
          public void onFailure(String errorMsg, Response response) {
          }
        });
      }
    }
  }

  public static void updateCancel() {
    timer.cancel();
  }
}