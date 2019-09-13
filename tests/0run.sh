#!/bin/sh -e

rc=0

./t_feature_static_inaccessible.sh     || rc=$?
./t_regression_changed_inaccessible.sh || rc=$?

if [ "$rc" = 0 ]; then
	echo "[ OK ] TESTS PASSED"
else
	echo "[FAIL] TESTS FAILED"
fi
