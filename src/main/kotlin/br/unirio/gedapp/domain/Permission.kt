package br.unirio.gedapp.domain

enum class Permission(val level: PermissionLevel, val default: Boolean) {
    SEARCH_DOCS         (PermissionLevel.DEPARTMENT, true),
    ADD_DOCS            (PermissionLevel.DEPARTMENT, false),
    EDIT_DOCS_OTHERS    (PermissionLevel.DEPARTMENT, false),
    DELETE_DOCS_OTHERS  (PermissionLevel.DEPARTMENT, false),
    INVITE_USERS        (PermissionLevel.DEPARTMENT, false),
    MANAGE_CATEGORIES   (PermissionLevel.DEPARTMENT, false),
    MANAGE_DEPT_PERM    (PermissionLevel.DEPARTMENT, false),
    MANAGE_DEPARTMENTS  (PermissionLevel.SYSTEM, false),
    MANAGE_SYSTEM_PERM  (PermissionLevel.SYSTEM, false);

    companion object {
        private fun getPermissionsByLevel(level: PermissionLevel) = values().filter { it.level == level }

        fun getDefaultPermissions() = values().filter { it.default }

        fun getDepartmentPermissions() = getPermissionsByLevel(PermissionLevel.DEPARTMENT)

        fun getSystemPermissions() = getPermissionsByLevel(PermissionLevel.SYSTEM)
    }
}