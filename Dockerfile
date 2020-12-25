FROM gradle:6.7.1-jdk11
COPY ./ /or2p
WORKDIR /or2p
RUN gradle build
CMD gradle run --args="-T ./src/test/resources/mini-map.pl"
