*! wid v0.0.0.9000 Thomas Blanchet 24may2017

// Copyright (C) 2017 Thomas Blanchet
// 
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

program wid
	version 13
	
	syntax, [INDicators(string) AReas(string) Years(numlist) Perc(string) AGes(string) POPulation(string) METAdata clear]
	
	// ---------------------------------------------------------------------- //
	// Check if there are already some data in memory
	// ---------------------------------------------------------------------- //
	
	quietly count
	if (r(N) > 0 & "`clear'" == "") {
		display as error "no; data in memory would be lost"
		exit 4
	}
	
	// ---------------------------------------------------------------------- //
	// Parse the arguments
	// ---------------------------------------------------------------------- //
	
	// If no area specified, use all of them
	if inlist("`areas'", "_all", "") {
		local areas "all"
	}
	else {
		// Add a comma between areas
		foreach a of local areas {
			if ("`areas_comma'" != "") {
				local areas_comma "`areas_comma',`a'"
			}
			else {
				local areas_comma "`a'"
			}
		}
		local areas `areas_comma'
	}
	// In no year specified, use all of them
	if inlist("`years'", "_all", "") {
		local years "1800-2015"
	}
	else {
		// Add a comma between years
		foreach y of local years {
			if ("`years_comma'" != "") {
				local years_comma "`years_comma',`y'"
			}
			else {
				local years_comma "`y'"
			}
		}
		local years `years_comma'
	}
	
	// ---------------------------------------------------------------------- //
	// Retrieve all possible variables for the area(s)
	// ---------------------------------------------------------------------- //
	
	display as text "* Get variables associated to your selection...", _continue
	
	javacall com.wid.WIDDownloader importCountriesAvailableVariables, args("`areas'")
	
	// Check if command was successful
	quietly count
	if (r(N) == 0) {
		display as error ""
		display as error "Cannot access the online WID.world database. Please:"
		display as error "  (1) Check that you internet connection works properly."
		display as error "  (2) If the problem persists, file bug report to thomas.blanchet@wid.world."
		exit 677
	}
	
	// ---------------------------------------------------------------------- //
	// Only keep variables that the user asked for
	// ---------------------------------------------------------------------- //
	
	quietly tab variable
	local nb_variable = r(r)
	quietly tab country
	local nb_country = r(r)
	quietly tab percentile
	local nb_percentile = r(r)
	quietly tab age
	local nb_age = r(r)
	
	
	
end
