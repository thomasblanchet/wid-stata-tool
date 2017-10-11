// Generates automatically the tables in the documentation

// Macro income

import excel "~/Dropbox/W2ID/Methodology/Codes_Dictionnary_WID.xlsx", ///
	sheet("Income_Macro_Variables") cellrange(B4:J137) clear

generate fivelet = B + C
generate descrip = strlower(J)
	
duplicates drop fivelet, force

generate line_stata = "{p2col: " + fivelet + "}" + descrip + "{p_end}"
generate line_R = "\code{" + fivelet + "} \tab      \tab " + descrip + "\cr"

keep line_*

// Distributed income

import excel "~/Dropbox/W2ID/Methodology/Codes_Dictionnary_WID.xlsx", ///
	sheet("Income_Distributed_Variables") cellrange(B4:J98) clear

generate fivelet = B + C
generate descrip = strlower(J)
	
duplicates drop fivelet, force

generate line_stata = "{p2col: " + fivelet + "}" + descrip + "{p_end}"
generate line_R = "\code{" + fivelet + "} \tab      \tab " + descrip + "\cr"

keep line_*

// Macro wealth

import excel "~/Dropbox/W2ID/Methodology/Codes_Dictionnary_WID.xlsx", ///
	sheet("Wealth_Macro_Variables") cellrange(B4:J154) clear

generate fivelet = B + C
generate descrip = strlower(J)
	
duplicates drop fivelet, force

generate line_stata = "{p2col: " + fivelet + "}" + descrip + "{p_end}"
generate line_R = "\code{" + fivelet + "} \tab      \tab " + descrip + "\cr"

keep line_*


// Distributed wealth

import excel "~/Dropbox/W2ID/Methodology/Codes_Dictionnary_WID.xlsx", ///
	sheet("Wealth_Distributed_Variables") cellrange(B4:J22) clear

generate fivelet = B + C
generate descrip = strlower(J)
	
duplicates drop fivelet, force

generate line_stata = "{p2col: " + fivelet + "}" + descrip + "{p_end}"
generate line_R = "\code{" + fivelet + "} \tab      \tab " + descrip + "\cr"

keep line_*

