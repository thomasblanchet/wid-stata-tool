*! wid_example3 v1.0.3 Thomas Blanchet 11oct2017

// Plot the long run evolution of average net national income per adult
// in US, FR, DE and GB, in log scale

program wid_example3
	version 13

	quietly {
		preserve

		// Download and store the 2016 USD PPP exchange rate
		wid, indicators(xlcusp) areas(FR US DE GB) year(2016) clear
		rename value ppp
		tempfile ppp
		save "`ppp'"

		// Download net national income in constant 2016 local currency
		wid, indicators(anninc) areas(FR US DE GB) age(992) clear
		merge n:1 country using "`ppp'", nogenerate

		// Convert to 2016 USD PPP (thousands)
		replace value = value/ppp/1000

		// Reshape and plot
		keep country year value
		reshape wide value, i(year) j(country) string
		label variable valueFR "France"
		label variable valueUS "United States"
		label variable valueDE "Germany"
		label variable valueGB "United Kingdom"

		graph twoway line value* year, yscale(log) ///
			ytitle("2016 PPP USD (000')") ylabel(2 5 10 20 50 100) ///
			title("Average net national income") subtitle("per adult") ///
			note("Source: WID.world")
		
		restore
	}
end
