#!/bin/sh -e
#
# TEST 2019/09/13
# This test passes as of JMBB 1.0.3

root="$(cd "$(dirname "$0")" && pwd)"
wd="/tmp/jmbbtest$$"
trap "[ -n \"$JMBB_TRACE\" ] || { chmod 777 -R \"$wd\"; rm -r $wd; }" \
								INT TERM EXIT

# prepare
mkdir "$wd"
mkdir "$wd/in" "$wd/in2" "$wd/out"
big4 -z "$wd/in/file1.bin"  29 MiB > "$wd/001file1.log"
big4 -b "$wd/in2/file2.bin" 29 MiB > "$wd/002file2.log"
echo testwort | jmbb -o "$wd/out" -i "$wd/in" "$wd/in2" > "$wd/003creat.log"

# update with "failure" directory
echo OVERWRITTEN STRING | dd of="$wd/in/file1.bin" conv=notrunc \
							> "$wd/004chg.log" 2>&1
chmod 000 "$wd/in/file1.bin"
rc1=0
"$root/p_invoke_jmbb.sh" -o "$wd/out" -i "$wd/in" "$wd/in2" \
					> "$wd/005jbbupdate.log" 2>&1 || rc1=$?

# attempt to restore
mkdir "$wd/restored"
rc2=0
"$root/p_invoke_jmbb.sh" -r "$wd/restored" -s "$wd/out" \
				> "$wd/006jmbbrestore.log" 2>&1 || rc2=$?

passfile=0
files="$(find "$wd/restored$wd" -type f -name '*.bin' \
				-exec basename {} \; | sort | tr '\n' ' ')"
if [ "$files" = "file1.bin file2.bin " ]; then
	if [ "$rc1" = 0 ]; then
		echo "[FAIL] t_regression_changed_inaccessible:" \
					"restore OK, but no fail indicated"
	elif [ "$rc2" = 0 ]; then
		echo "[ OK ] t_regression_changed_inaccessible"
		exit 0
	else
		echo "[FAIL] t_regression_changed_inaccessible:" \
					"restore OK, but fail indicated."
	fi
else
	echo "[FAIL] t_regression_changed_inaccessible: DATA LOST." \
					"Wrongly restored: \"$files\""
	
fi

# in general, exit with error code unless test passed.
exit 1
