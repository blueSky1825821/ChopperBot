import request from "@/utils/request";
import { Account } from "@/views/data/user/account";

// 获取所有用户
export function getAllUsers() {
  return request({
    url: "/account/getUser",
    method: "get",
  });
}

// 根据平台ID获取用户
export function getUsersByPlatform(platformId: number) {
  return request({
    url: `/account/getUser/${platformId}`,
    method: "get",
  });
}

// 添加账号
export function addAccount(platformId: number, username: string) {
  return request({
    url: `/account/login/${platformId}`,
    method: "post",
    params: { username },
  });
}

// 编辑账号
export function editAccount(account: Account) {
  return request({
    url: "/account/edit",
    method: "post",
    data: account,
  });
}

// 删除账号
export function deleteAccount(uid: number) {
  return request({
    url: "/account/edit",
    method: "post",
    data: { 
      id: uid,
      status: 0 // 假设0为删除状态
    },
  });
}

// 绑定频道
export function bindChannel(userId: string, channelId: string) {
  return request({
    url: `/account/bind/${userId}/${channelId}`,
    method: "post",
  });
}

// 解绑频道
export function unbindChannel(userId: string, channelId: string) {
  return request({
    url: `/account/bind/${userId}/${channelId}`,
    method: "delete",
  });
}