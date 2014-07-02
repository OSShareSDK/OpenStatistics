/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\ÐÂµÄSVN\\AnalysisSDK\\branches\\Analysis SDK 0.0.1\\AnalysisSDK\\src\\cn\\sharesdk\\analysis\\server\\AIDLService.aidl
 */
package cn.sharesdk.analysis.server;
public interface AIDLService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements cn.sharesdk.analysis.server.AIDLService
{
private static final java.lang.String DESCRIPTOR = "cn.sharesdk.analysis.server.AIDLService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an cn.sharesdk.analysis.server.AIDLService interface,
 * generating a proxy if needed.
 */
public static cn.sharesdk.analysis.server.AIDLService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof cn.sharesdk.analysis.server.AIDLService))) {
return ((cn.sharesdk.analysis.server.AIDLService)iin);
}
return new cn.sharesdk.analysis.server.AIDLService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_setting:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.setting(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_saveLog:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.saveLog(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_uploadLog:
{
data.enforceInterface(DESCRIPTOR);
this.uploadLog();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements cn.sharesdk.analysis.server.AIDLService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void setting(java.lang.String action, java.lang.String value) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(action);
_data.writeString(value);
mRemote.transact(Stub.TRANSACTION_setting, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void saveLog(java.lang.String action, java.lang.String jsonString) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(action);
_data.writeString(jsonString);
mRemote.transact(Stub.TRANSACTION_saveLog, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void uploadLog() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_uploadLog, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_setting = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_saveLog = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_uploadLog = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void setting(java.lang.String action, java.lang.String value) throws android.os.RemoteException;
public void saveLog(java.lang.String action, java.lang.String jsonString) throws android.os.RemoteException;
public void uploadLog() throws android.os.RemoteException;
}
