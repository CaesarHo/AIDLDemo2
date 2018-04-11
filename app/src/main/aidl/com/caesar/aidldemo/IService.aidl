// IService.aidl
package com.caesar.aidldemo;
import com.caesar.aidldemo.MessageCenter;

interface IService {
  void registerCallback(MessageCenter cb);
  void unregisterCallback(MessageCenter cb);
}
