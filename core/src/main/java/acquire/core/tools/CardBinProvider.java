package acquire.core.tools;

import android.text.TextUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import acquire.base.BaseApplication;
import acquire.base.utils.StringUtils;
import acquire.core.constant.CardOrg;
import acquire.core.constant.FileConst;

/**
 * Card bin utils
 *
 * @author Janson
 * @date 2022/7/19 17:28
 */
public class CardBinProvider {
    private final static List<String[]> CARD_BINS = new ArrayList<>();

    /**
     * Init
     */
    private static void init() {
        //load card bin file
        try (InputStream is = BaseApplication.getAppContext().getAssets().open(FileConst.CARD_BIN)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dombuild = factory.newDocumentBuilder();
            Document dom = dombuild.parse(is);
            Element root = dom.getDocumentElement();
            //Node: organization
            NodeList organizations = root.getChildNodes();
            for (int i = 0; i < organizations.getLength(); i++) {
                Node orgItem = organizations.item(i);
                if (!(orgItem instanceof Element)) {
                    continue;
                }
                String name = ((Element) orgItem).getAttribute("name");
                if (name == null) {
                    continue;
                }
                String organization;
                switch (name) {
                    case "CUP":
                        organization = CardOrg.CUP;
                        break;
                    case "VISA":
                        organization = CardOrg.VISA;
                        break;
                    case "MASTER CARD":
                        organization = CardOrg.MAE;
                        break;
                    case "AMEX":
                        organization = CardOrg.AMEX;
                        break;
                    case "JCB":
                        organization = CardOrg.JCB;
                        break;
                    case "DISCOVER":
                        organization = CardOrg.DISCOVER;
                        break;
                    default:
                        continue;
                }
                NodeList cardBins = orgItem.getChildNodes();
                for (int j = 0; j < cardBins.getLength(); j++) {
                    Node cardBin = cardBins.item(j);
                    if (!(cardBin instanceof Element)) {
                        continue;
                    }
                    String start = ((Element) cardBin).getAttribute("start").trim();
                    String end = ((Element) cardBin).getAttribute("end").trim();
                    CARD_BINS.add(new String[]{start, end, organization});
                }
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }

    }


    /**
     * get card organization
     *
     * @param pan card number
     * @return card organization {@link CardOrg}
     */
    public static String getCardOrg(String pan) {
        if (CARD_BINS.isEmpty()) {
            init();
        }
        for (String[] cardBin : CARD_BINS) {
            String start = cardBin[0];
            String end = cardBin[1];
            if (belong(start, end, pan)) {
                return cardBin[2];
            }
        }
        return null;
    }

    private static boolean belong(String start, String end, String pan) {
        if (TextUtils.isEmpty(end)) {
            return pan.startsWith(start);
        } else {
            try {
                int maxLen = Math.max(start.length(), end.length());
                start = StringUtils.fill(start, "0", maxLen, false);
                end = StringUtils.fill(end, "0", maxLen, false);
                BigInteger nStart=new BigInteger(start);
                BigInteger nEnd=new BigInteger(end);
                BigInteger nPan;
                if (pan.length() > maxLen) {
                    nPan = new BigInteger(pan.substring(0, maxLen));
                } else {
                    nPan = new BigInteger(StringUtils.fill(pan, "0", maxLen, false));
                }
                if (nPan.compareTo(nStart) > 0 && nPan.compareTo(nEnd) < 0) {
                    return true;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return false;
        }

    }

}
