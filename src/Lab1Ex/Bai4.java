package Lab1Ex;

import javacard.framework.*;

/***

	/select 112233445603
	
==================================================

	MaSV: CT060330 -> Lc = Le = 08
	
	/send "00 00 00 00 08 43 54 30 36 30 33 33 30"
					   Lc C  T  0  6  0  3  3  0
	Expected output: 90 00
	
	/send "00 f0 00 00 08"
	Expected output: 43 54 30 36 30 33 33 30 90 00
	
==================================================

	hoTen = Phuoc -> Lc = Le = 05
	
	/send "00 01 00 00 05 50 68 75 6F 63"
					   Lc P  h  u  o  c
	Expected output: 90 00
	
	/send "00 f1 00 00 05"
	Expected output: 50 68 75 6F 63 90 00
				
==================================================

	ngaySinh = 23/02/2003 -> Lc = Le = 10 -> 0A
	
	/send "00 02 00 00 0A 32 33 2F 30 32 2F 32 30 30 33"
					   Lc  2  3  /  0  2  /  2  0  0  3
	Expected output: 90 00		
	
	/send "00 f2 00 00 0A"
	Expected output: 32 33 2F 30 32 2F 32 30 30 33 90 00

==================================================

	queQuan = Ha Noi -> Lc = Le = 06
	
	/send "00 03 00 00 06 48 61 20 4E 6F 69"
					   Lc H  a     N  o  i
	Expected output: 90 00
	
	/send "00 f3 00 00 06"
	Expected output: 48 61 20 4E 6F 69 90 00
	
==================================================
	
	Get all -> Le = 8 + 5 + 10 + 6 + 3 (Seperator) = 32 -> 20
	
	/send "00 ff 00 00 20"
	Expected output: 43 54 30 36 30 33 33 30 7C 50 68 75 6F 63 7C 32 33 2F 30 32 2F 32 30 30 33 7C 48 61 20 4E 6F 69 90 00
					 C  T  0  6  0  3  3  0  |  P  h  u  o  c  |  2  3  /  0  2  /  2  0  0  3  |  H  a     N  o  i
	
==================================================

	Get data without input first
	
	/send "00 f0 00 00"
			  f1
			  f2
			  f3
			  ff
	Expected output:  69 84   Reference data not usable
					 
***/

public class Bai4 extends Applet
{
	private static final byte INS_SET_MASV = (byte)0x00;
	private static final byte INS_SET_HOTEN = (byte)0x01;
	private static final byte INS_SET_NGAYSINH = (byte)0x02;
	private static final byte INS_SET_QUEQUAN = (byte)0x03;
	
	private static final byte INS_GET_MASV = (byte)0xf0;
	private static final byte INS_GET_HOTEN = (byte)0xf1;
	private static final byte INS_GET_NGAYSINH = (byte)0xf2;
	private static final byte INS_GET_QUEQUAN = (byte)0xf3;
	private static final byte INS_GET_ALL = (byte)0xff;

	private byte[] maSV;
	private byte[] hoTen;
	private byte[] ngaySinh;
	private byte[] queQuan;

	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new Bai4().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	public void process(APDU apdu)
	{
		if (selectingApplet())
		{
			return;
		}

		byte[] buf = apdu.getBuffer();
		
		switch (buf[ISO7816.OFFSET_INS])
		{
		case INS_SET_MASV:
			maSV = receiveData(apdu, buf);
			break;
		case INS_SET_HOTEN:
			hoTen = receiveData(apdu, buf);
			break;
		case INS_SET_NGAYSINH:
			ngaySinh = receiveData(apdu, buf);
			break;
		case INS_SET_QUEQUAN:
			queQuan = receiveData(apdu, buf);
			break;
			
		case INS_GET_MASV:
			sendData(apdu, buf, maSV);
			break;
		case INS_GET_HOTEN:
			sendData(apdu, buf, hoTen);
			break;
		case INS_GET_NGAYSINH:
			sendData(apdu, buf, ngaySinh);
			break;
		case INS_GET_QUEQUAN:
			sendData(apdu, buf, queQuan);
			break;

		case INS_GET_ALL:
			sendAllData(apdu, buf);
			break;
			
		default:
			ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}


	private byte[] receiveData(APDU apdu, byte[] buf) {
		apdu.setIncomingAndReceive();

		short lc = (short)(buf[ISO7816.OFFSET_LC] & 0x00FF);
		short dataOffset = ISO7816.OFFSET_CDATA;
		
		byte[] newArray = new byte[lc];
		Util.arrayCopy(buf, dataOffset, newArray, (short) 0, lc);
		
		return newArray;
	}


	private void sendData(APDU apdu, byte[] buf, byte[] data) {
		if (data == null) {
			ISOException.throwIt(ISO7816.SW_DATA_INVALID);
		}
		
		short len = (short) data.length;
		Util.arrayCopy(data, (short) 0, buf, (short) 0, len);
		apdu.setOutgoingAndSend((short) 0, len);
	}

	private void sendAllData(APDU apdu, byte[] buf) {
		if (maSV == null || hoTen == null || ngaySinh == null || queQuan == null) {
			ISOException.throwIt(ISO7816.SW_DATA_INVALID); 
		}
		
		byte[] separator = { '|' };
		short sepLen = (short) separator.length;
		short offset = 0;
		
		short maSvLen = (short) maSV.length;
		Util.arrayCopy(maSV, (short) 0, buf, offset, maSvLen);
		offset += maSvLen;
		
		Util.arrayCopy(separator, (short) 0, buf, offset, sepLen);
		offset += sepLen;

		short hoTenLen = (short) hoTen.length;
		Util.arrayCopy(hoTen, (short) 0, buf, offset, hoTenLen);
		offset += hoTenLen;

		Util.arrayCopy(separator, (short) 0, buf, offset, sepLen);
		offset += sepLen;
		
		short ngaySinhLen = (short) ngaySinh.length;
		Util.arrayCopy(ngaySinh, (short) 0, buf, offset, ngaySinhLen);
		offset += ngaySinhLen;
		
		Util.arrayCopy(separator, (short) 0, buf, offset, sepLen);
		offset += sepLen;
		
		short queQuanLen = (short) queQuan.length;
		Util.arrayCopy(queQuan, (short) 0, buf, offset, queQuanLen);
		offset += queQuanLen;
		
		apdu.setOutgoingAndSend((short) 0, offset);
	}
}