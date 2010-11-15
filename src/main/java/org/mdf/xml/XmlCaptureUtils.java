package org.mdf.xml;

import java.util.ArrayList;
import java.util.List;

import org.mdf.mockdata.generated.Param;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlCaptureUtils {

	public static List<Param> nodesToParam(Node element,Param parent) {
		ArrayList<Param> paramList = new ArrayList<Param>();
		NodeList nodes = element.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName() != "#text") {
				Param param = new Param();
				param.setName(node.getNodeName());
				param.setValue(node.getNodeValue());
				NamedNodeMap attributes = node.getAttributes();
				param.setParam(nodesToParam(node,param).toArray(new Param[0]));
				if (attributes != null) {
					if (attributes.getLength() > 0) {
						ArrayList<Param> attribParams = new ArrayList<Param>();
						Param attribParam = new Param();
						attribParam.setName("xml attributes");

						for (int j = 0; j < attributes.getLength(); j++) {
							Node attributeNode = attributes.item(j);
							Param attribute = new Param();
							attribute.setName(attributeNode.getNodeName());
							attribute.setValue(attributeNode.getNodeValue());
							attribParams.add(attribute);
						}
						attribParam
								.setParam(attribParams.toArray(new Param[0]));
						param.addParam(attribParam);
					}
				}
				paramList.add(param);
			}
			else
				parent.setValue(node.getNodeValue());
			

		}
		return paramList;
	}
	
	public static List<Param> documentToParams(Document doc)
	{
		List<Param> params = new ArrayList<Param>();
		Element docElement = doc.getDocumentElement();
		Param print = new Param();
		params = nodesToParam(docElement,print);
		print.setName(docElement.getNodeName());
		print.setParam(params.toArray(new Param[0]));
		NamedNodeMap attributes = docElement.getAttributes();
		if (attributes != null) {
			if (attributes.getLength() > 0) {
				ArrayList<Param> attribParams = new ArrayList<Param>();
				Param attribParam = new Param();
				attribParam.setName("xml attributes");

				for (int j = 0; j < attributes.getLength(); j++) {
					Node attributeNode = attributes.item(j);
					Param attribute = new Param();
					attribute.setName(attributeNode.getNodeName());
					attribute.setValue(attributeNode.getNodeValue());
					attribParams.add(attribute);
				}
				attribParam
						.setParam(attribParams.toArray(new Param[0]));
				print.addParam(attribParam);
			}
		}
		ArrayList<Param> retParams = new ArrayList<Param>();
		retParams.add(print);
		return retParams;
		
	}	

}
