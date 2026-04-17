#!/bin/bash

cpu=$(top -bn1 | grep "Cpu(s)" | awk '{print $2 + $4}')

echo "$cpu%"

