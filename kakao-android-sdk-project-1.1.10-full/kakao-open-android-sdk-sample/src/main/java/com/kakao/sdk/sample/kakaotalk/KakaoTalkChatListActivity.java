package com.kakao.sdk.sample.kakaotalk;

import android.app.Application;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.kakao.kakaotalk.ChatListContext;
import com.kakao.kakaotalk.KakaoTalkService;
import com.kakao.kakaotalk.KakaoTalkService.ChatType;
import com.kakao.kakaotalk.callback.TalkResponseCallback;
import com.kakao.kakaotalk.response.ChatListResponse;
import com.kakao.kakaotalk.response.model.ChatInfo;
import com.kakao.network.ErrorResult;
import com.kakao.sdk.sample.R;
import com.kakao.sdk.sample.common.BaseActivity;
import com.kakao.sdk.sample.common.GlobalApplication;
import com.kakao.sdk.sample.common.log.Logger;
import com.kakao.sdk.sample.common.widget.KakaoDialogSpinner;
import com.kakao.sdk.sample.common.widget.KakaoToast;
import com.kakao.sdk.sample.friends.FriendsMainActivity.MSG_TYPE;

import java.util.ArrayList;
import java.util.List;

/**
 * @author by leoshin on 15. 8. 25..
 */
public class KakaoTalkChatListActivity extends BaseActivity implements IChatListCallback {

    private static class ChatInfos {
        private final List<ChatInfo> chatInfoList = new ArrayList<ChatInfo>();
        private int totalCount;

        public ChatInfos() {
        }

        public List<ChatInfo> getChatInfoList() {
            return chatInfoList;
        }

        public void merge(ChatListResponse response) {
            this.totalCount = response.getTotalCount();
            this.chatInfoList.addAll(response.getChatInfoList());
        }

        public int getTotalCount() {
            return totalCount;
        }
    }

    private ListView chatListView;
    private ChatListAdapter adapter = null;
    private ChatListContext chatListContext;
    private ChatInfos chatInfos;
    protected KakaoDialogSpinner msgType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_chat_list);

        msgType = (KakaoDialogSpinner)findViewById(R.id.message_type);
        chatListView = (ListView)findViewById(R.id.chat_list);
        requestChatLists();
    }

    public class ChatListAdapter extends BaseAdapter {
        private class ViewHolder {
            NetworkImageView profileView;
            TextView title;
        }
        private List<ChatInfo> items = null;
        private IChatListCallback listener = null;

        public ChatListAdapter(List<ChatInfo> items, IChatListCallback listener) {
            this.items = items;
            this.listener = listener;
        }

        public void setItem(List<ChatInfo> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ChatInfo getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (position == getCount() - 5) {
                if (listener != null) {
                    listener.onPreloadNext();
                }
            }

            View layout = convertView;
            ViewHolder holder;
            if (layout == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                layout = inflater.inflate(R.layout.view_chatroom_item, null, false);
                holder = new ViewHolder();
                holder.profileView = (NetworkImageView)layout.findViewById(R.id.chatroom_thumbnail);
                holder.profileView.setDefaultImageResId(R.drawable.thumb_story);
                holder.profileView.setErrorImageResId(R.drawable.thumb_story);
                holder.title = (TextView)layout.findViewById(R.id.title);

                layout.setTag(holder);
            } else {
                holder = (ViewHolder)layout.getTag();
            }

            final ChatInfo info = getItem(position);
            Application app  = GlobalApplication.getGlobalApplicationContext();
            String profileUrl = info.getImageUrl();
            if (profileUrl != null && profileUrl.length() > 0) {
                holder.profileView.setImageUrl(profileUrl, ((GlobalApplication) app).getImageLoader());
            } else {
                holder.profileView.setImageResource(R.drawable.thumb_story);
            }

            String nickName = info.getTitle();
            if (nickName != null && nickName.length() > 0) {
                holder.title.setVisibility(View.VISIBLE);
                holder.title.setText(nickName);
            } else {
                holder.title.setVisibility(View.INVISIBLE);
            }

            layout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemSelected(position, info);
                    }
                }
            });
            return layout;
        }
    }

    private void requestChatLists() {
        adapter = null;
        chatListContext = ChatListContext.createContext(ChatType.MULTI, 0, 30, "asc");
        chatInfos = new ChatInfos();
        requestMultiChatListInner();
    }

    private void requestMultiChatListInner() {
        final IChatListCallback callback = this;
        KakaoTalkService.requestChatList(new KakaoTalkChatListResponseCallback<ChatListResponse>() {
            @Override
            public void onSuccess(ChatListResponse result) {
                if (result != null) {
                    if (result.getChatInfoList().size() == 0) {
                        KakaoToast.makeToast(self, "Not exist ChatRoom", Toast.LENGTH_LONG).show();
                        return;
                    }

                    chatInfos.merge(result);
                    if (adapter == null) {
                        adapter = new ChatListAdapter(chatInfos.getChatInfoList(), callback);
                        chatListView.setAdapter(adapter);
                    } else {
                        adapter.setItem(chatInfos.getChatInfoList());
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }, chatListContext);
    }

    @Override
    public void onItemSelected(final int position, final ChatInfo chatInfo) {
        TalkMessageHelper.showSendMessageDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestSendMessage(MSG_TYPE.valueOf(msgType.getSelectedItemPosition()), chatInfo);
            }
        });
    }

    @Override
    public void onPreloadNext() {
        if (chatListContext.hasNext()) {
            requestMultiChatListInner();
        }
    }

    private void requestSendMessage(MSG_TYPE msgType, ChatInfo chatInfo) {
        KakaoTalkMessageBuilder builder = new KakaoTalkMessageBuilder();
        if (msgType == MSG_TYPE.NORMAL) {
            builder.addParam("username", "Your name is " + chatInfo.getTitle());
            builder.addParam("labelMsg", "This is Chat room test messagae");
            builder.addParam("linkMsg", "Go to website");
        }
        KakaoTalkService.requestSendMessage(new KakaoTalkChatListResponseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Logger.d("++ send message result : " + result);
                KakaoToast.makeToast(self, "Send message success", Toast.LENGTH_LONG).show();
            }
        }, chatInfo, TalkMessageHelper.getSampleTemplateId(msgType), builder.build());
    }

    public abstract class KakaoTalkChatListResponseCallback<T> extends TalkResponseCallback<T> {

        @Override
        public void onNotKakaoTalkUser() {
            KakaoToast.makeToast(self, "not a KakaoTalk user", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(ErrorResult errorResult) {
            KakaoToast.makeToast(self, "failure : " + errorResult, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onSessionClosed(ErrorResult errorResult) {
            redirectLoginActivity();
        }

        @Override
        public void onNotSignedUp() {
            redirectSignupActivity();
        }

        @Override
        public void onDidStart() {
            showWaitingDialog();
        }

        @Override
        public void onDidEnd() {
            cancelWaitingDialog();
        }
    }
}
