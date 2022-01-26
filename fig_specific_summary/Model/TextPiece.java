package Model;
import org.apache.pdfbox.pdmodel.font.PDFont;


/**
 * A textpiece is a sequence of characters that have the same font size/style
 * @author aum
 *
 */

public class TextPiece {
	int pageNum = 0;
	float fontHeight = 0;	//use height as font size
	PDFont font = null;	//font style
	
}
