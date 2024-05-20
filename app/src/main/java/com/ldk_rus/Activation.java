/*****************************************************************************
*
* RUS program for protected application
*
* Copyright (C) 2021 Thales Group. All rights reserved.
*
* Please export this class to JAR file
*
*****************************************************************************/

package com.ldk_rus;

import android.annotation.SuppressLint;

import com.safenet.patch.Product;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


/**
 * This class contains all the business logic needed for the activation.<br/>
 * <p/>
 * This class also handles self signed certificate on https connections.
 * <p/>
 */
@SuppressLint("DefaultLocale")
public class Activation 
{
    public static final int HASP_STATUS_OK = 0;

    /**Required header for WS calls*/
    private final static String VERSION = "application/vnd.ems.v12";

    /**WS URI*/
    public static final String PK_LOGIN_URL = "ems/v21/ws/loginByProductKey.ws";

    /**Error message constants*/
    public static final String  MSG_BAD_CONNECTION = "Sentinel EMS is not available for the specified URL";


    /**
     * Handle self signed certificates
     */
    static
    {
        try
        {
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
            SSLContext.setDefault(ctx);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     * Creates a connection either http or ssl depending on the protocol specified in the
     * URL
     * @param url
     * @param isHttps
     * @return
     * @throws Exception
     */
    protected static HttpURLConnection createConnection(URL url, boolean isHttps)throws Exception
    {
        if ( !isHttps )
        {
            return (HttpURLConnection)url.openConnection();
        }
        else
        {
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            if ( conn == null )
            {
                Data.appendMsg("create connect failed.");
            }

            //for handling user generated certificates, which are not CA verified
            //so we override the HostnameVerifier in the connection so host will not
            //be verified with the certificate host
            conn.setHostnameVerifier(new HostnameVerifier() 
            {
                @Override
                public boolean verify(String arg0, SSLSession arg1) 
                {
                    return true;
                }
            });

            return (HttpURLConnection)conn;
        }
    }


    /**
     * Updates the key with the license (v2c)
     * @param v2c
     * @return
     * @throws Exception
     */
    public static boolean updateKeyWithLicense(String v2c)throws Exception
    {
        Product product = new Product();
        product.Update(v2c);

        int status = product.getLastError();

        if ( status != HASP_STATUS_OK )
        {
            Data.appendMsg("Error: updateKeyWithLicense Fails with status code " + status);
            return false;
        }
        else
        {
            return true;
        }
    }


    /**
     * invokes the EMS login web service
     * @param serverUrl
     * @param product key
     * 
     * @return
     */
    public  static String[] customerLogin(String serverUrl, String pk)throws Exception
    {
        URL url = new URL(serverUrl + '/' + PK_LOGIN_URL);
        DataOutputStream out = null;
        BufferedReader in = null;

        boolean https = isHttps(serverUrl);
        HttpURLConnection conn = null;

        String body = "productKey=" + pk;
        BufferedReader errIn = null;

        try
        {
            conn = createConnection(url, https);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", VERSION);
            conn.connect();
            out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(body);
            out.flush();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line = null;

            while( (line = in.readLine()) != null )
            {
                sb.append(line);
                sb.append('\n');
            }

            return extractJsessionIdFromResponese(sb.toString());
        }
        catch(Exception e)
        {
            Data.appendMsg("Error customerLogin: " + e.getMessage());
            if ( conn != null )
            {
                try
                {
                    int respCode = conn.getResponseCode();
                    Data.appendMsg("customerLogin returned http code = " + respCode);

                    if ( respCode == 405 )
                    {
                        throw new Exception (MSG_BAD_CONNECTION);
                    }

                    errIn = new BufferedReader(new InputStreamReader(conn.getErrorStream()));

                }
                catch(Exception ex)
                {
                    throw new Exception(MSG_BAD_CONNECTION);
                }

                StringBuffer sb = new StringBuffer();
                String line = null;

                while( (line = errIn.readLine()) != null )
                {
                    sb.append(line);
                    sb.append('\n');
                }

                String errMsg = sb.toString();
                throw new Exception(errMsg,e);
            }

            throw e;

        }
        finally
        {
            if ( out != null )
            {
                out.close();
            }

            if ( in != null )
            {
                in.close();
            }

            if( conn != null )
            {
                conn.disconnect();
            }
        }

    }


    /**
     * extracts the jsessionid and the registration flag from the login response
     * @param xml
     * @return
     */
    protected static String[] extractJsessionIdFromResponese(String xml)throws Exception
    {
        String[] loginRes = new String[3];
        boolean b = xml.contains("<html>");
        if ( b )
        {
            return null;
        }

        InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(is);

        //get the jsession id
        NodeList nl = doc.getElementsByTagName("sessionId");
        String sessionId = null;

        if ( nl != null && nl.getLength() > 0 )
        {
            for ( int i = 0; i < nl.getLength(); ++i )
            {
                if ( nl.item(0).getNodeType() == Node.ELEMENT_NODE )
                {
                    sessionId = ((Element)nl.item(0)).getTextContent();
                    loginRes[0] = sessionId;
                }
            }
        }

        //see if registration is required
        NodeList nlReg = doc.getElementsByTagName("regRequired");
        String reg = null;

        if ( nlReg != null && nlReg.getLength() > 0 )
        {
            for ( int i = 0; i < nlReg.getLength(); ++i )
            {
                if ( nlReg.item(0).getNodeType() == Node.ELEMENT_NODE )
                {
                    reg = ((Element)nlReg.item(0)).getTextContent();
                    loginRes[1] = reg;
                }
            }
        }

        //see if redirect To UserReg needed
        NodeList nlRedirectToUserReg = doc.getElementsByTagName("redirectToUserReg");
        String redirectToUserReg = null;

        if ( nlRedirectToUserReg != null && nlRedirectToUserReg.getLength() > 0 )
        {
            for ( int i = 0; i < nlRedirectToUserReg.getLength(); ++i )
            {
                if ( nlRedirectToUserReg.item(0).getNodeType() == Node.ELEMENT_NODE )
                {
                    redirectToUserReg = ((Element)nlRedirectToUserReg.item(0)).getTextContent();
                    loginRes[2] = redirectToUserReg;
                }
            }
        }

        return loginRes;
    }


    /**
     * checks http/https protocol
     * @param serverUrl
     * @return
     */
    protected static boolean isHttps( String serverUrl )
    {
        boolean b = serverUrl.toLowerCase().startsWith("https");
        if ( b )
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    /**
     * 
     * @param jsessionId
     * @param c2v
     * @param product key
     * @return
     */
    public static String getLicense(String serverUrl,String jsessionId, String c2v, String pk)throws Exception
    {
        String requestBody = generateLicenseRequestXml(c2v);
        URL url = new URL(serverUrl + "/ems/v21/ws/productKey/" + pk + "/activation.ws");

        OutputStream out = null;
        BufferedReader in = null;

        boolean https = isHttps(serverUrl);
        HttpURLConnection conn = null;
        BufferedReader errIn = null;

        try
        {
            conn = createConnection(url,https);
            conn.toString();

            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", VERSION);
            conn.setRequestProperty("Cookie", "JSESSIONID=" + jsessionId);
            conn.setRequestProperty("Content-type", "application/xml");

            out = conn.getOutputStream();
            out.write(requestBody.getBytes());
            out.flush();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            if ( conn.getResponseCode() == 200 )
            {
                StringBuffer sb = new StringBuffer();
                String line = null;
                while ( (line = in.readLine()) != null )
                {
                    sb.append(line);
                    sb.append('\n');
                }

                String responseXml = sb.toString();
                return exctractV2CFromResponseXml(responseXml);
            }
            else
            {
                throw new Exception("server returned error code " + conn.getResponseCode());
            }

        }
        catch(Exception e)
        {
            Data.appendMsg("Error getting license: " + e.getMessage());

            if(conn != null)
            {
                errIn = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuffer sb = new StringBuffer();
                String line = null;

                while( ( line = errIn.readLine() ) != null )
                {
                    sb.append(line);
                    sb.append('\n');
                }

                String errMsg = sb.toString();
                throw new Exception(errMsg,e);

            }

            throw e;
        }
        finally
        {
            if ( out != null )
            {
                out.close();
            }

            if ( in != null )
            {
                in.close();
            }

            if ( errIn != null )
            {
                errIn.close();
            }

            if ( conn != null )
            {
                conn.disconnect();
            }
        }
    }


    /**
     * 
     * @param responseXml
     * @return
     * @throws Exception
     */
    protected static String exctractV2CFromResponseXml(String responseXml)throws Exception
    {
        InputStream is = new ByteArrayInputStream(responseXml.getBytes("UTF-8"));
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(is);

        //get the jsession id
        NodeList nl = doc.getElementsByTagName("activationString");
        String activationString = null;

        if ( nl != null && nl.getLength() > 0 )
        {
            for ( int i = 0; i < nl.getLength(); ++i )
            {
                if ( nl.item(0).getNodeType() == Node.ELEMENT_NODE )
                {
                    activationString = ((Element)nl.item(0)).getTextContent();
                    return activationString;
                }
            }
        }

        return null;
    }


    /**
     * Creates an WS request xml containing the c2v
     * @param c2v
     * @return
     */
    protected static String generateLicenseRequestXml(String c2v)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"); 
        sb.append("<activation xsi:noNamespaceSchemaLocation=\"License.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"); 
        sb.append("<activationInput>\n");
        sb.append("<activationAttribute>\n");
        sb.append("<attributeValue>\n");
        sb.append("<![CDATA[");
        sb.append(c2v);
        sb.append("]]> \n");
        sb.append("</attributeValue>\n");
        sb.append("<attributeName>C2V</attributeName> \n");
        sb.append(" </activationAttribute>\n");
        sb.append("<comments></comments> \n");
        sb.append("</activationInput>\n");
        sb.append("</activation>\n");

        return sb.toString();
    }


    /**
     * For handling user generated certificates
     * 
     *
     */
    private static class DefaultTrustManager implements X509TrustManager 
    {
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {  return null;  }
    }

}
