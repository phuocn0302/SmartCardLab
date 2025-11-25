package Lab4Ex;

import javacard.framework.*;

public class GradeManager extends Applet
{
	final static byte SV_ID_LENGTH = (byte)0x04;
	private static byte[] diemThi, sinhVien;
	private static byte soLuongMonThi;
	private static byte soLuongDaNhap;

	private final static byte INS_INFO = (byte)0x00;
	private final static byte INS_NHAP = (byte)0x10;
	private final static byte INS_IN = (byte)0x11;
	private final static byte INS_SUA = (byte)0x12;
	private final static byte INS_XOA = (byte)0x13;

	public static void install(byte[] bArray, short bOffset, byte bLength)
	{
		new GradeManager(bArray, bOffset, bLength);
	}

	private GradeManager(byte[] bArray, short bOffset, byte bLength)
	{
		byte iLen = bArray[bOffset];
		if (iLen == 0)
			register();
		else
			register(bArray, (short)(bOffset + 1), iLen);

		bOffset = (short)(bOffset + iLen + 1);
		byte cLen = bArray[bOffset];
		bOffset = (short)(bOffset + cLen + 1);
		byte aLen = bArray[bOffset];
		bOffset = (short)(bOffset + 1);

		if (aLen != 0)
		{
			sinhVien = new byte[SV_ID_LENGTH];
			Util.arrayCopy(bArray, bOffset, sinhVien, (short)0, SV_ID_LENGTH);
			bOffset += SV_ID_LENGTH;
			soLuongMonThi = bArray[bOffset];
		}
		else
		{
			sinhVien = new byte[]{ 'S', 'V', '0', '1' };
			soLuongMonThi = (byte)0x09;
		}

		diemThi = new byte[(short)(soLuongMonThi * 2)];
		soLuongDaNhap = 0;
	}

	public void process(APDU apdu)
	{
		if (selectingApplet()) return;

		byte[] buf = apdu.getBuffer();
		apdu.setIncomingAndReceive();

		switch (buf[ISO7816.OFFSET_INS])
		{
			case INS_INFO:
				apdu.setOutgoing();
				apdu.setOutgoingLength((short)5);
				apdu.sendBytesLong(sinhVien, (short)0, SV_ID_LENGTH);
				buf[0] = soLuongMonThi;
				apdu.sendBytes((short)0, (short)1);
				break;

			case INS_NHAP:
				nhapDiem(buf);
				inDiem(apdu);
				break;

			case INS_IN:
				inDiem(apdu);
				break;

			case INS_SUA:
				suaDiem(buf);
				inDiem(apdu);
				break;

			case INS_XOA:
				xoaDiem(buf);
				inDiem(apdu);
				break;

			default:
				ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

	private void nhapDiem(byte[] buf)
	{
		byte id = buf[5];
		byte diem = buf[6];

		for (byte i = 0; i < soLuongDaNhap; i++)
			if (diemThi[(short)(i * 2)] == id)
				ISOException.throwIt((short)0x6A80);

		if (soLuongDaNhap >= soLuongMonThi)
			ISOException.throwIt((short)0x6A84);

		short p = (short)(soLuongDaNhap * 2);
		diemThi[p] = id;
		diemThi[(short)(p + 1)] = diem;
		soLuongDaNhap++;
	}

	private void suaDiem(byte[] buf)
	{
		byte id = buf[5];
		byte diem = buf[6];

		for (byte i = 0; i < soLuongDaNhap; i++)
		{
			short p = (short)(i * 2);
			if (diemThi[p] == id)
			{
				diemThi[(short)(p + 1)] = diem;
				return;
			}
		}
		ISOException.throwIt((short)0x6A88);
	}

	private void xoaDiem(byte[] buf)
	{
		byte id = buf[5];

		for (byte i = 0; i < soLuongDaNhap; i++)
		{
			short p = (short)(i * 2);
			if (diemThi[p] == id)
			{
				for (byte j = i; j < (byte)(soLuongDaNhap - 1); j++)
				{
					short a = (short)(j * 2);
					short b = (short)((j + 1) * 2);
					diemThi[a] = diemThi[b];
					diemThi[(short)(a + 1)] = diemThi[(short)(b + 1)];
				}
				soLuongDaNhap--;
				return;
			}
		}
		ISOException.throwIt((short)0x6A88);
	}

	private void inDiem(APDU apdu)
	{
		byte[] buf = apdu.getBuffer();
		short len = (short)(soLuongDaNhap * 2);
		Util.arrayCopy(diemThi, (short)0, buf, (short)0, len);
		apdu.setOutgoingAndSend((short)0, len);
	}
}
