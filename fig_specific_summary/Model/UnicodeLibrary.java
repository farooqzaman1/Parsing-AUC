package Model;

//collection of useful UTF-8 unicodes
public class UnicodeLibrary {
	public static final char[] SYMBOLS = {
		'\u0021', '\u0022', '\u0023', '\u0024', '\u0025', '\u0026', '\'', '\u0028', '\u0029', 
		'\u002A', '\u002B', '\u002C', '\u002D', '\u002E', '\u002F', '\u003A', '\u003B', '\u003C', 
		'\u003D', '\u003E', '\u003F', '\u0040', '\u005B', '\\', '\u005D', '\u005E', '\u005F', 
		'\u0060', '\u007B', '\u007C', '\u007D', '\u007E', '\u00A1', '\u00A2', '\u00A3', '\u00A4', 
		'\u00A5', '\u00A6', '\u00A7', '\u00A8', '\u00A9', '\u00AA', '\u00AB', '\u00AC', '\u00AD', 
		'\u00AE', '\u00AF', '\u00B0', '\u00B1', '\u00B2', '\u00B3', '\u00B4', '\u00B5', '\u00B6', 
		'\u00B7', '\u00B8', '\u00B9', '\u00BA', '\u00BB', '\u00BC', '\u00BD', '\u00BE', '\u00BF', 
		'\u00C0', '\u00C1', '\u00C2', '\u00C3', '\u00C4', '\u00C5', '\u00C6', '\u00C7', '\u00C8', 
		'\u00C9', '\u00CA', '\u00CB', '\u00CC', '\u00CD', '\u00CE', '\u00CF', '\u00D0', '\u00D1', 
		'\u00D2', '\u00D3', '\u00D4', '\u00D5', '\u00D6', '\u00D7', '\u00D8', '\u00D9', '\u00DA', 
		'\u00DB', '\u00DC', '\u00DD', '\u00DE', '\u00DF', '\u00E0', '\u00E1', '\u00E2', '\u00E3', 
		'\u00E4', '\u00E5', '\u00E6', '\u00E7', '\u00E8', '\u00E9', '\u00EA', '\u00EB', '\u00EC', 
		'\u00ED', '\u00EE', '\u00EF', '\u00F0', '\u00F1', '\u00F2', '\u00F3', '\u00F4', '\u00F5', 
		'\u00F6', '\u00F7', '\u00F8', '\u00F9', '\u00FA', '\u00FB', '\u00FC', '\u00FD', '\u00FE', 
		'\u00FF',
		'∈'
	};
	
	public static final char[] GREEKCHARS = {
			'\u0370', '\u0371', '\u0372', '\u0373', '\u0374', '\u0375', '\u0376', '\u0377', '\u0378', '\u0379', '\u037A', '\u037B', '\u037C', '\u037D', '\u037E', '\u037F', '\u0380', '\u0381', '\u0382', '\u0383', '\u0384', '\u0385', '\u0386', '\u0387', '\u0388', '\u0389', '\u038A', '\u038B', '\u038C', '\u038D', '\u038E', '\u038F', '\u0390', '\u0391', '\u0392', '\u0393', '\u0394', '\u0395', '\u0396', '\u0397', '\u0398', '\u0399', '\u039A', '\u039B', '\u039C', '\u039D', '\u039E', '\u039F', '\u03A0', '\u03A1', '\u03A2', '\u03A3', '\u03A4', '\u03A5', '\u03A6', '\u03A7', '\u03A8', '\u03A9', '\u03AA', '\u03AB', '\u03AC', '\u03AD', '\u03AE', '\u03AF', '\u03B0', '\u03B1', '\u03B2', '\u03B3', '\u03B4', '\u03B5', '\u03B6', '\u03B7', '\u03B8', '\u03B9', '\u03BA', '\u03BB', '\u03BC', '\u03BD', '\u03BE', '\u03BF', '\u03C0', '\u03C1', '\u03C2', '\u03C3', '\u03C4', '\u03C5', '\u03C6', '\u03C7', '\u03C8', '\u03C9', '\u03CA', '\u03CB', '\u03CC', '\u03CD', '\u03CE', '\u03CF', '\u03D0', '\u03D1', '\u03D2', '\u03D3', '\u03D4', '\u03D5', '\u03D6', '\u03D7', '\u03D8', '\u03D9', '\u03DA', '\u03DB', '\u03DC', '\u03DD', '\u03DE', '\u03DF', '\u03E0', '\u03E1', '\u03E2', '\u03E3', '\u03E4', '\u03E5', '\u03E6', '\u03E7', '\u03E8', '\u03E9', '\u03EA', '\u03EB', '\u03EC', '\u03ED', '\u03EE', '\u03EF', '\u03F0', '\u03F1', '\u03F2', '\u03F3', '\u03F4', '\u03F5', '\u03F6', '\u03F7', '\u03F8', '\u03F9', '\u03FA', '\u03FB', '\u03FC', '\u03FD', '\u03FE', '\u03FF', '\u1F00', '\u1F01', '\u1F02', '\u1F03', '\u1F04', '\u1F05', '\u1F06', '\u1F07', '\u1F08', '\u1F09', '\u1F0A', '\u1F0B', '\u1F0C', '\u1F0D', '\u1F0E', '\u1F0F', '\u1F10', '\u1F11', '\u1F12', '\u1F13', '\u1F14', '\u1F15', '\u1F16', '\u1F17', '\u1F18', '\u1F19', '\u1F1A', '\u1F1B', '\u1F1C', '\u1F1D', '\u1F1E', '\u1F1F', '\u1F20', '\u1F21', '\u1F22', '\u1F23', '\u1F24', '\u1F25', '\u1F26', '\u1F27', '\u1F28', '\u1F29', '\u1F2A', '\u1F2B', '\u1F2C', '\u1F2D', '\u1F2E', '\u1F2F', '\u1F30', '\u1F31', '\u1F32', '\u1F33', '\u1F34', '\u1F35', '\u1F36', '\u1F37', '\u1F38', '\u1F39', '\u1F3A', '\u1F3B', '\u1F3C', '\u1F3D', '\u1F3E', '\u1F3F', '\u1F40', '\u1F41', '\u1F42', '\u1F43', '\u1F44', '\u1F45', '\u1F46', '\u1F47', '\u1F48', '\u1F49', '\u1F4A', '\u1F4B', '\u1F4C', '\u1F4D', '\u1F4E', '\u1F4F', '\u1F50', '\u1F51', '\u1F52', '\u1F53', '\u1F54', '\u1F55', '\u1F56', '\u1F57', '\u1F58', '\u1F59', '\u1F5A', '\u1F5B', '\u1F5C', '\u1F5D', '\u1F5E', '\u1F5F', '\u1F60', '\u1F61', '\u1F62', '\u1F63', '\u1F64', '\u1F65', '\u1F66', '\u1F67', '\u1F68', '\u1F69', '\u1F6A', '\u1F6B', '\u1F6C', '\u1F6D', '\u1F6E', '\u1F6F', '\u1F70', '\u1F71', '\u1F72', '\u1F73', '\u1F74', '\u1F75', '\u1F76', '\u1F77', '\u1F78', '\u1F79', '\u1F7A', '\u1F7B', '\u1F7C', '\u1F7D', '\u1F7E', '\u1F7F', '\u1F80', '\u1F81', '\u1F82', '\u1F83', '\u1F84', '\u1F85', '\u1F86', '\u1F87', '\u1F88', '\u1F89', '\u1F8A', '\u1F8B', '\u1F8C', '\u1F8D', '\u1F8E', '\u1F8F', '\u1F90', '\u1F91', '\u1F92', '\u1F93', '\u1F94', '\u1F95', '\u1F96', '\u1F97', '\u1F98', '\u1F99', '\u1F9A', '\u1F9B', '\u1F9C', '\u1F9D', '\u1F9E', '\u1F9F', '\u1FA0', '\u1FA1', '\u1FA2', '\u1FA3', '\u1FA4', '\u1FA5', '\u1FA6', '\u1FA7', '\u1FA8', '\u1FA9', '\u1FAA', '\u1FAB', '\u1FAC', '\u1FAD', '\u1FAE', '\u1FAF', '\u1FB0', '\u1FB1', '\u1FB2', '\u1FB3', '\u1FB4', '\u1FB5', '\u1FB6', '\u1FB7', '\u1FB8', '\u1FB9', '\u1FBA', '\u1FBB', '\u1FBC', '\u1FBD', '\u1FBE', '\u1FBF', '\u1FC0', '\u1FC1', '\u1FC2', '\u1FC3', '\u1FC4', '\u1FC5', '\u1FC6', '\u1FC7', '\u1FC8', '\u1FC9', '\u1FCA', '\u1FCB', '\u1FCC', '\u1FCD', '\u1FCE', '\u1FCF', '\u1FD0', '\u1FD1', '\u1FD2', '\u1FD3', '\u1FD4', '\u1FD5', '\u1FD6', '\u1FD7', '\u1FD8', '\u1FD9', '\u1FDA', '\u1FDB', '\u1FDC', '\u1FDD', '\u1FDE', '\u1FDF', '\u1FE0', '\u1FE1', '\u1FE2', '\u1FE3', '\u1FE4', '\u1FE5', '\u1FE6', '\u1FE7', '\u1FE8', '\u1FE9', '\u1FEA', '\u1FEB', '\u1FEC', '\u1FED', '\u1FEE', '\u1FEF', '\u1FF0', '\u1FF1', '\u1FF2', '\u1FF3', '\u1FF4', '\u1FF5', '\u1FF6', '\u1FF7', '\u1FF8', '\u1FF9', '\u1FFA', '\u1FFB', '\u1FFC', '\u1FFD', '\u1FFE', '\u1FFF'
	};
	
	public static final char[] ARROWS = {
			'\u2190', '\u2191', '\u2192', '\u2193', '\u2194', '\u2195', '\u2196', '\u2197', '\u2198', '\u2199', '\u219A', '\u219B', '\u219C', '\u219D', '\u219E', '\u219F', '\u21A0', '\u21A1', '\u21A2', '\u21A3', '\u21A4', '\u21A5', '\u21A6', '\u21A7', '\u21A8', '\u21A9', '\u21AA', '\u21AB', '\u21AC', '\u21AD', '\u21AE', '\u21AF', '\u21B0', '\u21B1', '\u21B2', '\u21B3', '\u21B4', '\u21B5', '\u21B6', '\u21B7', '\u21B8', '\u21B9', '\u21BA', '\u21BB', '\u21BC', '\u21BD', '\u21BE', '\u21BF', '\u21C0', '\u21C1', '\u21C2', '\u21C3', '\u21C4', '\u21C5', '\u21C6', '\u21C7', '\u21C8', '\u21C9', '\u21CA', '\u21CB', '\u21CC', '\u21CD', '\u21CE', '\u21CF', '\u21D0', '\u21D1', '\u21D2', '\u21D3', '\u21D4', '\u21D5', '\u21D6', '\u21D7', '\u21D8', '\u21D9', '\u21DA', '\u21DB', '\u21DC', '\u21DD', '\u21DE', '\u21DF', '\u21E0', '\u21E1', '\u21E2', '\u21E3', '\u21E4', '\u21E5', '\u21E6', '\u21E7', '\u21E8', '\u21E9', '\u21EA', '\u21EB', '\u21EC', '\u21ED', '\u21EE', '\u21EF', '\u21F0', '\u21F1', '\u21F2', '\u21F3', '\u21F4', '\u21F5', '\u21F6', '\u21F7', '\u21F8', '\u21F9', '\u21FA', '\u21FB', '\u21FC', '\u21FD', '\u21FE', '\u21FF'
	};
	
	public static final char[] MATHOPERATORS = {
			'\u2200', '\u2201', '\u2202', '\u2203', '\u2204', '\u2205', '\u2206', '\u2207', '\u2208', '\u2209', '\u220A', '\u220B', '\u220C', '\u220D', '\u220E', '\u220F', '\u2210', '\u2211', '\u2212', '\u2213', '\u2214', '\u2215', '\u2216', '\u2217', '\u2218', '\u2219', '\u221A', '\u221B', '\u221C', '\u221D', '\u221E', '\u221F', '\u2220', '\u2221', '\u2222', '\u2223', '\u2224', '\u2225', '\u2226', '\u2227', '\u2228', '\u2229', '\u222A', '\u222B', '\u222C', '\u222D', '\u222E', '\u222F', '\u2230', '\u2231', '\u2232', '\u2233', '\u2234', '\u2235', '\u2236', '\u2237', '\u2238', '\u2239', '\u223A', '\u223B', '\u223C', '\u223D', '\u223E', '\u223F', '\u2240', '\u2241', '\u2242', '\u2243', '\u2244', '\u2245', '\u2246', '\u2247', '\u2248', '\u2249', '\u224A', '\u224B', '\u224C', '\u224D', '\u224E', '\u224F', '\u2250', '\u2251', '\u2252', '\u2253', '\u2254', '\u2255', '\u2256', '\u2257', '\u2258', '\u2259', '\u225A', '\u225B', '\u225C', '\u225D', '\u225E', '\u225F', '\u2260', '\u2261', '\u2262', '\u2263', '\u2264', '\u2265', '\u2266', '\u2267', '\u2268', '\u2269', '\u226A', '\u226B', '\u226C', '\u226D', '\u226E', '\u226F', '\u2270', '\u2271', '\u2272', '\u2273', '\u2274', '\u2275', '\u2276', '\u2277', '\u2278', '\u2279', '\u227A', '\u227B', '\u227C', '\u227D', '\u227E', '\u227F', '\u2280', '\u2281', '\u2282', '\u2283', '\u2284', '\u2285', '\u2286', '\u2287', '\u2288', '\u2289', '\u228A', '\u228B', '\u228C', '\u228D', '\u228E', '\u228F', '\u2290', '\u2291', '\u2292', '\u2293', '\u2294', '\u2295', '\u2296', '\u2297', '\u2298', '\u2299', '\u229A', '\u229B', '\u229C', '\u229D', '\u229E', '\u229F', '\u22A0', '\u22A1', '\u22A2', '\u22A3', '\u22A4', '\u22A5', '\u22A6', '\u22A7', '\u22A8', '\u22A9', '\u22AA', '\u22AB', '\u22AC', '\u22AD', '\u22AE', '\u22AF', '\u22B0', '\u22B1', '\u22B2', '\u22B3', '\u22B4', '\u22B5', '\u22B6', '\u22B7', '\u22B8', '\u22B9', '\u22BA', '\u22BB', '\u22BC', '\u22BD', '\u22BE', '\u22BF', '\u22C0', '\u22C1', '\u22C2', '\u22C3', '\u22C4', '\u22C5', '\u22C6', '\u22C7', '\u22C8', '\u22C9', '\u22CA', '\u22CB', '\u22CC', '\u22CD', '\u22CE', '\u22CF', '\u22D0', '\u22D1', '\u22D2', '\u22D3', '\u22D4', '\u22D5', '\u22D6', '\u22D7', '\u22D8', '\u22D9', '\u22DA', '\u22DB', '\u22DC', '\u22DD', '\u22DE', '\u22DF', '\u22E0', '\u22E1', '\u22E2', '\u22E3', '\u22E4', '\u22E5', '\u22E6', '\u22E7', '\u22E8', '\u22E9', '\u22EA', '\u22EB', '\u22EC', '\u22ED', '\u22EE', '\u22EF', '\u22F0', '\u22F1', '\u22F2', '\u22F3', '\u22F4', '\u22F5', '\u22F6', '\u22F7', '\u22F8', '\u22F9', '\u22FA', '\u22FB', '\u22FC', '\u22FD', '\u22FE', '\u22FF'
			, '≥', '‖'
	};
	
	public static void main(String[] args)
	{
		//System.out.print("\u2022".equals("•"));
	}
}
