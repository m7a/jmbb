package ma.jmbb;

class IRStats {

	int databaseY = 0;
	int databaseN = 0;

	int activeY   = 0;
	int activeN   = 0;
	int activeAny = 0;

	int hddY      = 0;
	int hddN      = 0;
	int hddAny    = 0;

	int equalY    = 0;
	int equalN    = 0;
	int equalAny  = 0;

	int goodY     = 0;
	int goodN     = 0;

	void print(PrintfIO o) {
		o.printf("\n\nStatistics\n\n");
		o.printf("Section    NumYes (:)    " +
					"NumNo (E)     NumAny (_)    Sum\n");
		o.printf("Database?  %-12d  %-12d  -             %-12d\n",
				databaseY, databaseN, databaseY + databaseN);
		o.printf("Active?    %-12d  %-12d  %-12d  %-12d\n",
				activeY, activeN, activeAny,
				activeY + activeN + activeAny);
		o.printf("HDD?       %-12d  %-12d  %-12d  %-12d\n",
			hddY, hddN, hddAny, hddY + hddN + hddAny);
		o.printf("Equal?     %-12d  %-12d  %-12d  %-12d\n",
			equalY, equalN, equalAny, equalY + equalN + equalAny);
		o.printf("Good?      %-12d  %-12d  -             %-12d\n",
				goodY, goodN, goodY + goodN);
	}

}
