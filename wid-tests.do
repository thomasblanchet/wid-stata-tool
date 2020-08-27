// -------------------------------------------------------------------------- //
// Unit test for the WID Stata command
// -------------------------------------------------------------------------- //

clear all

global root "~/GitHub/wid-stata-tool"

sysdir set PERSONAL "$root"

// -------------------------------------------------------------------------- //
// We can download mutliple indicators for a single country
// -------------------------------------------------------------------------- //

wid, ind(sfiinc aptinc) perc(p90p100 p20p30) areas(FR) years(1990/2000) clear

assert inrange(year, 1990, 2000)
assert country == "FR"
assert inlist(percentile, "p20p30", "p90p100")
assert inlist(substr(variable, 1, 6), "aptinc", "sfiinc")

// -------------------------------------------------------------------------- //
// We can download a single indicator for multiple countries
// -------------------------------------------------------------------------- //

wid, areas(FR US) ind(sptinc) perc(p90p100) age(992) pop(j) years(1990/2000) clear

assert percentile == "p90p100"
assert inlist(country, "FR", "US")
assert inrange(year, 1990, 2000)
assert substr(variable, 10, 1) == "j"
assert substr(variable, 7, 3) == "992"
assert substr(variable, 1, 6) == "sptinc"

// -------------------------------------------------------------------------- //
// We can download population data
// -------------------------------------------------------------------------- //

wid, area(DE) ind(npopul) clear

assert country == "DE"
assert percentile == "p0p100"
assert substr(variable, 1, 6) == "npopul"
assert inlist(substr(variable, 10, 1), "i", "f", "m")

// -------------------------------------------------------------------------- //
// We can download metadata
// -------------------------------------------------------------------------- //

wid, areas(FR) indicator(sptinc) perc(p99p100) age(992) pop(j) metadata clear

assert country == "FR"
assert countryname == "France"
assert variable == "sptinc992j"
assert percentile == "p99p100"
assert data_quality == "5"
assert imputation == "surveys and tax microdata"

// -------------------------------------------------------------------------- //
// We can exclude extrapolations/interpolations
// -------------------------------------------------------------------------- //

wid, areas(MZ) indicator(sptinc) perc(p99p100) age(992) pop(j) exclude clear

assert country == "MZ"
assert variable == "sptinc992j"
assert percentile == "p99p100"

tempfile mz_excl
save "`mz_excl'"

wid, areas(MZ) indicator(sptinc) perc(p99p100) age(992) pop(j) clear

assert country == "MZ"
assert variable == "sptinc992j"
assert percentile == "p99p100"

merge 1:1 country variable percentile year value age pop using "`mz_excl'", update

assert inlist(_merge, 1, 3)

count if _merge == 1
assert r(N) > 0

count if _merge == 3
assert r(N) > 0
