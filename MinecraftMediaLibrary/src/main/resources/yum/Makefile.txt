#!/usr/bin/make
#
# user-yum.sh
#
# User package installer for CentOS (and RedHat and Fedhora)
# Developped on CentOS 6.5
# You can use Miniconda as a complementary package manager.
# I recommend Miniconda 3 over Miniconda 2.
#
# # Usage
# To download one or several package:
#     make +screen
#     make +zsh +tcl
# To install all downloaded packages:
#     make install
#
# To print the configuration to use in your `.profile`:
#     make environment
#
# You can uninstall the whole system at any moment just by deleting the
# $(ROOT_D) directory (see configuration).
#
# Note that at the moment, the system doesn't support package removal so any
# addition is definitive.
#
#  * If you did `make +the_wrong_package_name` but did not do `make install`,
#    you can still cancel the installation by dropping your cache:
#
#    (replace ROOT_D by the value you configured)
#    `rm -r $ROOT_D/rpm $ROOT_D/dwnldlist`
#
#    You can also use `make unload`. It does the same.
#
#    Either way, you'll lose your cache so 

# /\ Configuration beginning

# The installation prefix (The `+` in `+zsh`) can be configured or removed
INSTALL_FLAG_PREFIX := +

# ROOT_D, Root of the system
# ROOT_D := $(shell echo $$HOME)/y
ROOT_D := $(PWD)/root

# One of ``, `.x86_64` or `.i686`
ARCH := 

# \/ Configuration end


# /\ Useless customisation beginning

# DWNL directory, lisf of resolved packages
DWNL_D := $(ROOT_D)/dwnldlist
# DONE directory, lisf of installed packages
DONE_D := $(ROOT_D)/donelist
# cpio directory, cache containing .cpio files
CPIO_D := $(ROOT_D)/cpio
# rpm directory, cache containing .cpio files
RPM_D := $(ROOT_D)/rpm

# \/ Useless customisation end

THIS_FILE := $(lastword $(MAKEFILE_LIST))
REINVOK := $(MAKE) --no-print-directory -f "$(THIS_FILE)"

SED_LIMIT := sed -ne '1,24 p;25 i...'
SED_LIM   := sed -ne '1,12 p;13 i...'
SED_LI    := sed -ne '1,6  p;7  i...'

# SED_LOOP  := sed -n $$'1,13p;14i(...)\n:a;$$p;14,$$N;27,$$D;14,$$ba'
# SED_LOO   := sed -n $$'1,7p;8  i(...)\n:a;$$p;8,$$ N;15,$$D;8 ,$$ba'
# SED_LO    := sed -n $$'1,3p;4  i(...)\n:a;$$p;4,$$ N;7,$$ D;4 ,$$ba'

SED_LOOP  := sed -n $$'1,15p;:a;31i...\n$$bz;16,$$N;16,$$D;16,$$ba;:z;16,$$p'
SED_LOO   := sed -n $$'1,7 p;:a;15i...\n$$bz;8,$$ N;8,$$ D;8,$$ ba;:z;8,$$ p'
SED_LO    := sed -n $$'1,3 p;:a;7 i...\n$$bz;4,$$ N;7,$$ D;4,$$ ba;:z;4,$$ p'

YSUBDIRS := $(DWNL_D) $(DONE_D) $(CPIO_D) $(RPM_D)

SHELL := bash

nothing: .PHONY
	@echo "$(shell basename "$${PWD%.d}"): Nothing to install"

$(ROOT_D):
	mkdir $@

$(YSUBDIRS): | $(ROOT_D)
	mkdir $@

install:
	@echo "<< make update-cpio >>"
	@$(REINVOK) update-cpio
	@echo
	@echo "<< make update-usr >>"
	@$(REINVOK) update-usr

unload:
	-rm -r "$(DWNL_D)"
	-rm -r "$(RPM_D)"

environment env:
	@echo '# Setting environment for $(ROOT_D)'
	@echo 'ROOT_D="$(ROOT_D)"'
	@echo
	@echo 'PATH="$$ROOT_D/usr/sbin:$$ROOT_D/usr/bin:$$ROOT_D/bin:$$PATH"'
	@echo
	@echo 'L="/lib:/lib64:/usr/lib:/usr/lib64"'
	@echo 'export LD_LIBRARY_PATH="$$L:$$ROOT_D/usr/lib:$$ROOT_D/usr/lib64"'

debug:
	@echo $(YSUBDIRS)

# Update the cpio cache from the rpm cache
update-cpio: .PHONY
	@\
	rpm_d="$(RPM_D)"; \
	for rpmfile in "$(RPM_D)"/*.rpm; do \
		# echo "rpmfile:: $$rpmfile"; \
		cpiofile="$(CPIO_D)$${rpmfile#$$rpm_d}"; \
		# echo "cpiofile:: $$cpiofile"; \
		cpiofile="$${cpiofile%.rpm}.cpio"; \
		# echo "cpiofile:: $$cpiofile"; \
		$(REINVOK) "$${cpiofile}"; \
	done 2>&1 | $(SED_LOOP)

# Uncompress all `.cpio` to y/* (usually y/usr and y/var)
update-usr: .PHONY
	@\
	cpio_d="$(CPIO_D)"; \
	for cpiofile in $(CPIO_D)/*.cpio; do \
		# echo "cpiofile:: $$cpiofile"; \
		donefile="$(DONE_D)$${cpiofile#$$cpio_d}"; \
		donefile="$${donefile%.cpio}.done"; \
		# echo "$${donefile}"; \
		$(REINVOK) "$${donefile}"; \
	done 2>&1 | $(SED_LOOP)


# `make *.cpio` Will just convert an EXISTING .rpm to a .cpio one in the cache.
$(CPIO_D)/%.cpio: | $(CPIO_D)
	@rpm2cpio "$(RPM_D)/$*.rpm" > "$@"

#	: $(CPIO_D)/$*
# Uncompress a specific .cpio file
$(DONE_D)/%.done: | $(DONE_D)
	@echo 'Uncompressing $*.cpio'
	@cd "$(ROOT_D)"; { cpio -id < "$(CPIO_D)/$*.cpio" 2>&3 \
	| $(SED_LO); } 3>&1 1>&2 \
	| $(SED_LOO) 2>&1
	@touch "$@"

# The below recipes handles installation of target files
# - If the file is in the DWNL_D cache, it is already installed
# - Otherwise, download it and it's dependencies
#   Then unpack all new available targets
$(INSTALL_FLAG_PREFIX)%:
	@$(REINVOK) "$(DWNL_D)"/"$*" 2>&1 | $(SED_LOOP)

$(DWNL_D)/%: | $(DWNL_D) $(RPM_D)
	@yumdownloader --destdir="$(RPM_D)" --resolve "$*"'$(ARCH)' 2>&1 \
	| grep -v '^Failed to set locale,\|^Loaded plugins\|^Loading mirror\| * \(base:\|extras\|updates\)\|^Nothing to download\|^--> ' \
	| tee >(export output; read -r -s -d'' output); \
	echo "$$output" | grep '^--->Package ' >/dev/null && touch "$@" || true
	@echo
.PHONY:
