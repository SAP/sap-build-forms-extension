package com.sap.bfx.utils;

/**
 * An abstract base implementation of the Identifier interface.
 */	
public abstract class AbstractBaseIdentifier implements Identifier {
    private final String identifier;

    /**
	 * Constructor for AbstractBaseIdentifier.
	 *
	 * @param identifier the identifier string
	 */
    protected AbstractBaseIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public java.lang.String getIdentifier() {
        return this.identifier;
    }

    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Identifier or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Identifier)) {
            return false;
        }

        // typecast o to Identifier so that we can compare data members
        Identifier i = (Identifier) o;

        // Compare the data members and return accordingly
        return i.getIdentifier().compareTo(this.getIdentifier()) == 0;
    }

    @Override
    public int hashCode() {
        return this.getIdentifier().hashCode();
    }
}
