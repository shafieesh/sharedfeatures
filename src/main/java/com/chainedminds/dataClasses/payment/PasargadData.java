package com.chainedminds.dataClasses.payment;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PasargadData {

    public String InvoiceNumber;
    public String InvoiceDate = new SimpleDateFormat("yyyy/MM/dd").format(new Date());;
    public int TerminalCode = 1901868;
    public int MerchantCode = 4711318;
    public int Amount;
    public String RedirectAddress;
    public String Timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
    public int Action = 1003;
    public boolean IsSuccess;
    public String Sign;
    public String Token;
    public String TransactionReferenceID;
}