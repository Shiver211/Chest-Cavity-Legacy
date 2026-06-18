package com.shiver.chestcavity.integration.crafttweaker.callback;

import com.shiver.chestcavity.integration.crafttweaker.context.OrganInsertContext;
import com.shiver.chestcavity.integration.crafttweaker.context.OrganRemoveContext;
import com.shiver.chestcavity.integration.crafttweaker.context.OrganTickContext;
import com.shiver.chestcavity.integration.crafttweaker.runtime.CtConstants;
import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;

public final class OrganCallbacks {

    private OrganCallbacks() {
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "organs.CanInsert")
    @ZenRegister
    public interface CanInsert {
        void handle(OrganInsertContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "organs.CanRemove")
    @ZenRegister
    public interface CanRemove {
        void handle(OrganRemoveContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "organs.OnInserted")
    @ZenRegister
    public interface OnInserted {
        void handle(OrganInsertContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "organs.OnRemoved")
    @ZenRegister
    public interface OnRemoved {
        void handle(OrganRemoveContext context);
    }

    @ZenClass(CtConstants.CT_NAMESPACE + "organs.OnTick")
    @ZenRegister
    public interface OnTick {
        void handle(OrganTickContext context);
    }
}
