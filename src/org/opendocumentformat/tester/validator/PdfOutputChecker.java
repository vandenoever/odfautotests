package org.opendocumentformat.tester.validator;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFImageWriter;
import org.example.documenttests.OutputReportType;
import org.example.documenttests.PdfType.Mask;
import org.example.documenttests.PdfinfoType;
import org.example.documenttests.PdfinfoType.MaskResult;
import org.example.documenttests.SimpleResultType;
import org.example.documenttests.ValidationErrorTypeType;
import org.opendocumentformat.tester.Tester;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;

public class PdfOutputChecker {

	public PdfOutputChecker() {
	}

	private boolean handleSvg(BufferedImage pdfimage, int resolutiondpi,
			Mask mask, String pngpath, String pngthumbpath) {
		DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
		String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
		Document doc = impl.createDocument(svgNS, "svg", null);
		Element svgRoot = doc.getDocumentElement();
		String width = (new Float(pdfimage.getWidth() / 1.0 / resolutiondpi))
				.toString();
		String height = (new Float(pdfimage.getHeight() / 1.0 / resolutiondpi))
				.toString();
		svgRoot.setAttributeNS(null, "width", width + "in");
		svgRoot.setAttributeNS(null, "height", height + "in");

		for (Object o : mask.getAny()) {
			svgRoot.appendChild(doc.importNode((Node) o, true));
		}
		BufferedImage svgimage = null;
		try {
			svgimage = renderToBitmap(doc, resolutiondpi);
		} catch (TranscoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// create the new image, canvas size is the max. of both image sizes
		int w = Math.max(pdfimage.getWidth(), svgimage.getWidth());
		int h = Math.max(pdfimage.getHeight(), svgimage.getHeight());
		BufferedImage combined = new BufferedImage(w, h, pdfimage.getType());

		// paint both images, preserving the alpha channels
		Graphics g = combined.getGraphics();
		g.drawImage(pdfimage, 0, 0, null);
		g.drawImage(svgimage, 0, 0, null);

		// check that the entire image is white
		boolean white = true;
		for (int y = 0; white && y < combined.getHeight(); y++) {
			for (int x = 0; white && x < combined.getWidth(); x++) {
				int color = combined.getRGB(x, y);
				// check that the color is white or transparent
				white &= ((color & 0xff000000) == 0)
						|| ((color & 0x00ffffff) == 16777215);
			}
		}
		try {
			ImageIO.write(combined, "png", new File(pngpath));
			// TODO : write thumb
		} catch (IOException e) {
			e.printStackTrace();
		}
		return white;
	}

	private BufferedImage renderToBitmap(Document doc, int resolutiondpi)
			throws TranscoderException, IOException {
		PNGTranscoder t = new PNGTranscoder();
		float millimetresPerPixel = 25.4f / resolutiondpi;
		t.addTranscodingHint(PNGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER,
				millimetresPerPixel);
		TranscoderInput input = new TranscoderInput(doc);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		TranscoderOutput output = new TranscoderOutput(out);
		t.transcode(input, output);
		out.flush();
		out.close();
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		return ImageIO.read(in);
	}

	final String pngNumberFormat = "-%01d.png";

	public void check(File pdfpath, OutputReportType report, List<Mask> masks) {
		PdfReader reader = null;
		try {
			reader = new PdfReader(pdfpath.getPath());
		} catch (IOException e) {
			OdfChecker.report(report.getValidation(),
					ValidationErrorTypeType.INVALIDPDFFILE, e.getMessage());
			return;
		}
		PdfinfoType info = new PdfinfoType();
		report.setPdfinfo(info);
		int n = reader.getNumberOfPages();
		info.setPages(n);
		String path = pdfpath.getPath();
		String pathbase = path.substring(0, path.lastIndexOf('.'));

		for (int i = 1; i <= n; ++i) {
			PdfinfoType.Page p = new PdfinfoType.Page();
			Rectangle r = reader.getPageSize(i);
			p.setHeight(r.getHeight());
			p.setWidth(r.getWidth());
			info.getPage().add(p);
			String png = String.format(pngNumberFormat, i);
			p.setPng(pathbase + png);
			p.setPngthumb(pathbase + "-thumb" + png);
		}
		int resolutiondpi = 150;
		String pdfpngpath = pathbase + String.format(pngNumberFormat, 1);
		createPngs(pdfpath, pathbase + "-", resolutiondpi, n);
		createPngs(pdfpath, pathbase + "-thumb-", resolutiondpi / 10, n);

		BufferedImage pdfimage = null;
		try {
			pdfimage = ImageIO.read(new File(pdfpngpath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<MaskResult> maskResults = info.getMaskResult();
		for (int i = 0; i < masks.size(); ++i) {
			MaskResult mr = new MaskResult();
			Mask m = masks.get(i);
			mr.setName(m.getName());
			if (pdfimage != null) {
				String pngpath = pathbase + "-" + m.getName() + ".png";
				String thumbpath = pathbase + "-" + m.getName() + "-thumb.png";
				mr.setPng(pngpath);
				mr.setPngthumb(thumbpath);
				boolean ok = handleSvg(pdfimage, resolutiondpi, m, pngpath,
						thumbpath);
				mr.setResult(ok ? SimpleResultType.TRUE
						: SimpleResultType.FALSE);
			} else {
				mr.setResult(SimpleResultType.INVALID);
			}
			maskResults.add(mr);
		}
	}

	private void createPngs(File pdfpath, String pngpath, int resolutiondpi,
			int numberOfPages) {
		String pdftoppm = Tester.resolveExe("pdftoppm");
		if (!pdftoppm.equals("pdftoppm")) {
			String cmd[] = new String[6];
			cmd[0] = pdftoppm;
			cmd[1] = "-r";
			cmd[2] = String.valueOf(resolutiondpi);
			cmd[3] = "-png";
			cmd[4] = pdfpath.getPath();
			cmd[5] = pngpath.substring(0, pngpath.length() - 1);
			Tester.runCommand(cmd, null);
			return;
		}
		PDDocument document = null;
		try {
			document = PDDocument.loadNonSeq(pdfpath, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		PDFImageWriter imageWriter = new PDFImageWriter();
		try {
			for (int i = 1; i <= numberOfPages; ++i) {
				String path = String.format(pngpath, i);
				// use TYPE_INT_ARGB instead of TYPE_INT_ARGB because otherwise
				// bitmaps from Abiword look bad
				imageWriter.writeImage(document, "png", null, i, i, path,
						BufferedImage.TYPE_INT_RGB, resolutiondpi);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			document.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
